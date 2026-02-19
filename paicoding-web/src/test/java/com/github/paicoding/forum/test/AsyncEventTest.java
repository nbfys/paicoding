package com.github.paicoding.forum.test;

import com.github.paicoding.forum.web.QuickForumApplication;
import com.github.paicoding.forum.service.article.service.event.ArticlePublishEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuickForumApplication.class)
@ComponentScan("com.github.paicoding.forum")
public class AsyncEventTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    public void testAsyncPublish() throws InterruptedException {
        System.out.println("1. [主线程] 准备发布事件...");

        // 模拟发布一篇文章 (假设ID=2)
        // 注意：确保数据库里真的有 id=2 的文章，或者你可以随便传个 id 和内容，因为 AiSummaryService 里如果查不到文章可能会报错，或者直接用传进去的 title/content
        // 为了稳妥，我们传真实的 title 和 content
        Long articleId = 1L; // 用昨天存在的 ID
        String title = "异步测试标题";
        String content = "这是一个用于测试 Spring Event 异步解耦的长文本内容...";

        eventPublisher.publishEvent(new ArticlePublishEvent(this, articleId, title, content));

        System.out.println("2. [主线程] 事件已发布，主线程继续执行，不等待 AI...");
        System.out.println("3. [主线程] 任务结束。");

        // 强行阻塞主线程，等待子线程跑完 (否则测试用例一结束，后台线程就被杀掉了)
        Thread.sleep(5000);
    }
}