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

@Slf4j
@Service
public class AiSummaryServiceImpl implements AiSummaryService {

    @Autowired
    private ArticleAiSummaryMapper summaryMapper;

    @Autowired
    private AiClient aiClient;

    @Override
    public String getSummary(Long articleId, String title, String content) {
        ArticleAiSummaryDO exist = summaryMapper.selectOne(
                new LambdaQueryWrapper<ArticleAiSummaryDO>()
                        .eq(ArticleAiSummaryDO::getArticleId, articleId)
        );

        // 只有状态为成功 (1) 才返回摘要，否则返回提示或 null
        if (exist != null && Integer.valueOf(1).equals(exist.getStatus())) {
            return exist.getSummary();
        }
        return null;
    }

    /**
     * 核心生成逻辑 (写逻辑，加上重试)
     * @Retryable: 遇到任何 Exception 重试，最多 3 次，间隔 2 秒
     */
    @Override
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void generateSummary(Long articleId, String title, String content) {
        log.info(">>> 开始生成摘要 (可能重试)... articleId={}", articleId);

        // 1. 调 AI (如果 AI 挂了，这里会抛异常，触发重试)
        String summary = aiClient.getSummary(title, content);

        if (summary == null || summary.isEmpty()) {
            throw new RuntimeException("AI 生成摘要为空，触发重试");
        }

        // 2. 入库 (先查后插/更新)
        saveOrUpdate(articleId, summary, 1, null);
        log.info("<<< 摘要生成并入库成功! articleId={}", articleId);
    }

    /**
     * 重试耗尽后的兜底方法 (Recover)
     */
    @Recover
    public void recover(Exception e, Long articleId, String title, String content) {
        log.error("!!! 重试 3 次均失败，放弃生成。articleId={}, err={}", articleId, e.getMessage());

        // 记录失败状态 (status=2) 和错误信息
        saveOrUpdate(articleId, "", 2, e.getMessage());
    }

    /**
     * 辅助方法：保存或更新记录
     */
    private void saveOrUpdate(Long articleId, String summary, Integer status, String errorMsg) {
        ArticleAiSummaryDO record = summaryMapper.selectOne(
                new LambdaQueryWrapper<ArticleAiSummaryDO>().eq(ArticleAiSummaryDO::getArticleId, articleId)
        );

        if (record == null) {
            record = new ArticleAiSummaryDO()
                    .setArticleId(articleId)
                    .setSummary(summary)
                    .setStatus(status)
                    .setErrorMsg(errorMsg == null ? "" : errorMsg.substring(0, Math.min(500, errorMsg.length())));
            summaryMapper.insert(record);
        } else {
            record.setSummary(summary)
                    .setStatus(status)
                    .setErrorMsg(errorMsg == null ? "" : errorMsg.substring(0, Math.min(500, errorMsg.length())));
            summaryMapper.updateById(record);
        }
    }
}