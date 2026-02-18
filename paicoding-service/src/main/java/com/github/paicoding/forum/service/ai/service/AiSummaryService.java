package com.github.paicoding.forum.service.ai.service;

public interface AiSummaryService {
    /**
     * 获取文章摘要（带缓存与生成逻辑）
     * @param articleId 文章ID
     * @param title 文章标题
     * @param content 文章内容
     * @return 摘要内容
     */
    String getSummary(Long articleId, String title, String content);
}