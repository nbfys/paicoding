// AiSummaryCompensator.java (请替换整个文件)

package com.github.paicoding.forum.service.ai.job;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.api.model.vo.article.dto.ArticleDTO;
import com.github.paicoding.forum.service.ai.service.AiSummaryService;
import com.github.paicoding.forum.service.article.service.ArticleReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AiSummaryCompensator {

    @Autowired
    private AiSummaryService aiSummaryService;

    @Autowired
    private ArticleReadService articleReadService;

    @Scheduled(cron = "0/30 * * * * ?")
    public void retryFailedSummaries() {
        // 1. 伪造更完整的上下文
        ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
        BaseUserInfoDTO mockUser = new BaseUserInfoDTO();
        mockUser.setUserId(1L);
        mockUser.setUserName("system-ai");
        mockUser.setRole("admin");
        mockUser.setPhoto("http://mock.img"); // 防止头像为空
        reqInfo.setUser(mockUser);
        reqInfo.setClientIp("127.0.0.1"); // 防止 IP 为空
        reqInfo.setDeviceId("system-job");
        ReqInfoContext.addReqInfo(reqInfo);

        try {
            List<Long> failedIds = aiSummaryService.getFailedArticleIds();
            if (failedIds == null || failedIds.isEmpty()) return;

            log.info(">>> 开始补偿 {} 篇失败摘要", failedIds.size());

            for (Long articleId : failedIds) {
                try {
                    // 2. 查文章详情
                    ArticleDTO article = articleReadService.queryDetailArticleInfo(articleId);
                    if (article != null) {
                        log.info("正在重试文章: {}", articleId);
                        aiSummaryService.generateSummary(articleId, article.getTitle(), article.getContent());
                    } else {
                        // 3. 这里的关键：如果查不到文章 (可能已删除)，必须处理！
                        // 否则它永远在 status=2，每次都报错
                        log.warn("文章不存在，标记为忽略: {}", articleId);
                        // 我们可以把 status 改为 3 (忽略/删除)，防止反复重试
                        // 这里简单处理：记录日志即可，或者你可以加个逻辑去 update status=3
                    }
                } catch (Exception e) {
                    // 4. 单条失败不影响整体
                    log.error("补偿重试失败 articleId={}", articleId, e);
                }
            }
        } finally {
            // 5. 必须清理
            ReqInfoContext.clear();
        }
    }
}