package com.github.paicoding.forum.service.article.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ArticlePublishEvent extends ApplicationEvent {

    private final Long articleId;
    private final String title;
    private final String content;

    public ArticlePublishEvent(Object source, Long articleId, String title, String content) {
        super(source);
        this.articleId = articleId;
        this.title = title;
        this.content = content;
    }
}