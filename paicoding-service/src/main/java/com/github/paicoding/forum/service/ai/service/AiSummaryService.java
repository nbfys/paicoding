package com.github.paicoding.forum.service.ai.service;

import java.util.List;

public interface AiSummaryService {
    /**
     * 获取文章摘要（带缓存与生成逻辑）
     * @param articleId 文章ID
     * @param title 文章标题
     * @param content 文章内容
     * @return 摘要内容
     */
    String getSummary(Long articleId, String title, String content);
    void generateSummary(Long articleId, String title, String content);
    //D45新增：获取生成失败的文章ID列表
    List<Long> getFailedArticleIds();
}