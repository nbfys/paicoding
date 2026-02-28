package com.github.paicoding.forum.web.front.ai;

import com.github.paicoding.forum.api.model.vo.ResVo;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.service.ai.service.AiSummaryService;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "AI 摘要服务", description = "提供文章摘要生成与查询能力") // 1. 分组标签
@RestController
@RequestMapping("/api/ai")
public class AiSummaryController {

    @Autowired
    private AiSummaryService aiSummaryService;

    @Autowired
    private ArticleReadService articleReadService;

    /**
     * 获取文章摘要
     * 这里的逻辑是：如果数据库有直接返回，没有则调用AI生成并入库
     */
    @ApiOperation(value = "获取文章AI摘要", notes = "根据文章ID查询摘要。若未生成过，系统将自动触发AI生成并入库，首次调用可能较慢(约3-5秒)。")
    @GetMapping("/summary")
    public ResVo<String> getSummary(
            @ApiParam(value = "文章ID", required = true, example = "1") // 2. 参数说明
            @RequestParam Long articleId) {

        // 1. 查文章详情
        ArticleDTO article = articleReadService.queryDetailArticleInfo(articleId);
        if (article == null) {
            return ResVo.fail(null, "文章不存在"); // 3. 错误码定义
        }

        // 2. 调业务逻辑
        String summary = aiSummaryService.getSummary(
                articleId,
                article.getTitle(),
                article.getContent()
        );

        return ResVo.ok(summary); // 4. 返回摘要文本
    }
}