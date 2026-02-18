package com.github.paicoding.forum.web.front.ai;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.service.ai.service.AiSummaryService;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiSummaryController {

    @Autowired
    private AiSummaryService aiSummaryService;

    // 需要注入文章读取服务，用来查文章详情
    @Autowired
    private ArticleReadService articleReadService;

    /**
     * 获取文章摘要
     */
    @GetMapping("/summary")
    public ResVo<String> getSummary(@RequestParam Long articleId) {
        // 1. 查文章详情 (为了拿到 title 和 content)
        ArticleDTO article = articleReadService.queryDetailArticleInfo(articleId);
        if (article == null) {
            return ResVo.fail(null, "文章不存在");
        }

        // 2. 调业务逻辑
        String summary = aiSummaryService.getSummary(
                articleId,
                article.getTitle(),
                article.getContent()
        );

        return ResVo.ok(summary);
    }
}