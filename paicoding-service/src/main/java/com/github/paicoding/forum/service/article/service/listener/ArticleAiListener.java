package com.github.paicoding.forum.service.article.service.listener;

import com.github.paicoding.forum.service.ai.service.AiSummaryService;
import com.github.paicoding.forum.service.article.service.event.ArticlePublishEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ArticleAiListener {

    @Autowired
    private AiSummaryService aiSummaryService;

    // @Async("taskExecutor") 指定使用我们自定义的线程池
    // @EventListener 监听 ArticlePublishEvent 事件
    @Async("taskExecutor")
    @EventListener
    public void onArticlePublish(ArticlePublishEvent event) {
        log.info(">>> 收到文章发布事件，开始异步生成摘要。ArticleId: {}, Thread: {}",
                event.getArticleId(), Thread.currentThread().getName());

        long start = System.currentTimeMillis();

        try {
            // 调用昨天的业务逻辑
            aiSummaryService.generateSummary(event.getArticleId(), event.getTitle(), event.getContent());

        } catch (Exception e) {
            log.error("异步生成摘要失败: {}", e.getMessage());
        }

        log.info("<<< 异步摘要生成结束。耗时: {}ms", System.currentTimeMillis() - start);
    }
}