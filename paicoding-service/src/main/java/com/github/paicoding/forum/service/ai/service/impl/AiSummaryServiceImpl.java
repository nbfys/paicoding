package com.github.paicoding.forum.service.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.paicoding.forum.service.ai.AiClient;
import com.github.paicoding.forum.service.ai.service.AiSummaryService;
import com.github.paicoding.forum.service.article.repository.entity.ArticleAiSummaryDO;
import com.github.paicoding.forum.service.article.repository.mapper.ArticleAiSummaryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiSummaryServiceImpl implements AiSummaryService {

    @Autowired
    private ArticleAiSummaryMapper summaryMapper;

    @Autowired
    private AiClient aiClient;

    /**
     * D45 新增：供定时任务调用
     * 查询所有 status = 2 (失败) 的记录，限制 100 条防止一次处理太多
     */
    @Override
    public List<Long> getFailedArticleIds() {
        return summaryMapper.selectList(new LambdaQueryWrapper<ArticleAiSummaryDO>()
                        .eq(ArticleAiSummaryDO::getStatus, 2) // 2 = 失败状态
                        .last("LIMIT 100"))
                .stream()
                .map(ArticleAiSummaryDO::getArticleId)
                .collect(Collectors.toList());
    }

    @Override
    public String getSummary(Long articleId, String title, String content) {
        ArticleAiSummaryDO exist = summaryMapper.selectOne(
                new LambdaQueryWrapper<ArticleAiSummaryDO>()
                        .eq(ArticleAiSummaryDO::getArticleId, articleId)
        );
        if (exist != null && Integer.valueOf(1).equals(exist.getStatus())) {
            return exist.getSummary();
        }
        return null;
    }

    /**
     * 核心生成逻辑
     * @Retryable: 遇到 RuntimeException 重试，最多 3 次，间隔 2 秒
     */
    @Override
    @Retryable(value = RuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void generateSummary(Long articleId, String title, String content) {
        log.info(">>> 开始生成摘要 (可能重试)... articleId={}", articleId);

        // 1. 调 AI
        String summary = aiClient.getSummary(title, content);

        // ✅ D45 关键修改：识别熔断降级文案
        // 如果 AiClient 返回了 fallback 文案，或者为空，我们都视为“失败”
        // 必须抛出异常，@Retryable 才会重试，最终触发 @Recover
        if (summary == null || summary.isEmpty()
                || summary.contains("系统繁忙")
                || summary.contains("不可用")
                || summary.contains("稍后再试")) {

            // 抛出异常 -> 触发重试 -> 最终触发 recover (status=2)
            throw new RuntimeException("AI 生成失败或触发熔断: " + summary);
        }

        // 2. 只有真正的摘要，才入库 (status=1)
        saveOrUpdate(articleId, summary, 1, null);
        log.info("<<< 摘要生成并入库成功! articleId={}", articleId);
    }

    /**
     * 重试耗尽后的兜底方法 (Recover)
     * 当 generateSummary 抛异常达到 3 次后，会进入这里
     */
    @Recover
    public void recover(RuntimeException e, Long articleId, String title, String content) {
        log.error("!!! 重试 3 次均失败，标记为失败状态。articleId={}, err={}", articleId, e.getMessage());

        // ✅ 记录失败状态 (status=2)，给定时任务留“线索”
        saveOrUpdate(articleId, "", 2, e.getMessage());
    }

    /**
     * 辅助方法：保存或更新记录
     * 逻辑：有则更新，无则插入
     */
    private void saveOrUpdate(Long articleId, String summary, Integer status, String errorMsg) {
        ArticleAiSummaryDO record = summaryMapper.selectOne(
                new LambdaQueryWrapper<ArticleAiSummaryDO>().eq(ArticleAiSummaryDO::getArticleId, articleId)
        );

        // 截取错误信息防止爆字段
        String safeErrorMsg = (errorMsg == null) ? "" : errorMsg.substring(0, Math.min(500, errorMsg.length()));

        if (record == null) {
            record = new ArticleAiSummaryDO()
                    .setArticleId(articleId)
                    .setSummary(summary)
                    .setStatus(status)
                    .setErrorMsg(safeErrorMsg);
            summaryMapper.insert(record);
        } else {
            record.setSummary(summary)
                    .setStatus(status)
                    .setErrorMsg(safeErrorMsg);
            summaryMapper.updateById(record);
        }
    }
}