package com.github.paicoding.forum.test;

import com.github.paicoding.forum.web.QuickForumApplication;
import com.github.paicoding.forum.service.article.repository.entity.ArticleAiSummaryDO;
import com.github.paicoding.forum.service.article.repository.mapper.ArticleAiSummaryMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuickForumApplication.class)
@ComponentScan("com.github.paicoding.forum")
public class ArticleAiSummaryTest {

    @Autowired
    private ArticleAiSummaryMapper summaryMapper;

    @Test
    public void testInsert() {
        long fakeArticleId = 9999L; // 伪造一个文章ID

        // 1. 清理旧数据 (防止主键冲突)
        summaryMapper.deleteByMap(java.util.Collections.singletonMap("article_id", fakeArticleId));

        // 2. 插入新数据
        ArticleAiSummaryDO record = new ArticleAiSummaryDO()
                .setArticleId(fakeArticleId)
                .setSummary("这是一段测试入库的摘要内容。");

        int rows = summaryMapper.insert(record);

        System.out.println(">>> 插入行数: " + rows);

        // 3. 查询验证
        ArticleAiSummaryDO saved = summaryMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ArticleAiSummaryDO>()
                        .eq(ArticleAiSummaryDO::getArticleId, fakeArticleId)
        );

        if (saved != null && saved.getSummary().contains("测试入库")) {
            System.out.println("✅ 数据库读写测试通过！");
        } else {
            System.err.println("❌ 数据库读写失败！");
        }
    }
}