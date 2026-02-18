package com.github.paicoding.forum.service.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.paicoding.forum.service.ai.AiClient;
import com.github.paicoding.forum.service.ai.service.AiSummaryService;
import com.github.paicoding.forum.service.article.repository.entity.ArticleAiSummaryDO;
import com.github.paicoding.forum.service.article.repository.mapper.ArticleAiSummaryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiSummaryServiceImpl implements AiSummaryService {

    @Autowired
    private ArticleAiSummaryMapper summaryMapper;

    @Autowired
    private AiClient aiClient;

    @Override
    public String getSummary(Long articleId, String title, String content) {
        // 1. 先查数据库
        ArticleAiSummaryDO exist = summaryMapper.selectOne(
                new LambdaQueryWrapper<ArticleAiSummaryDO>()
                        .eq(ArticleAiSummaryDO::getArticleId, articleId)
        );

        if (exist != null) {
            log.info("文章摘要命中数据库缓存: articleId={}", articleId);
            return exist.getSummary();
        }

        // 2. 数据库没有，调用 AI 生成
        log.info("文章摘要未命中，开始调用 AI 生成: articleId={}", articleId);
        String summary = aiClient.getSummary(title, content);

        // 3. AI 生成失败（返回 null），直接返回默认提示，不入库
        if (summary == null) {
            return "摘要生成中，请稍后再试...";
        }

        // 4. AI 生成成功，入库保存
        try {
            ArticleAiSummaryDO newRecord = new ArticleAiSummaryDO()
                    .setArticleId(articleId)
                    .setSummary(summary);
            summaryMapper.insert(newRecord);
            log.info("文章摘要入库成功: articleId={}", articleId);
        } catch (Exception e) {
            // 可能是并发导致重复插入，捕获异常，不要影响返回
            log.warn("文章摘要入库失败 (可能是并发重复): {}", e.getMessage());
        }

        return summary;
    }
}