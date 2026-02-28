package com.github.paicoding.forum.service.ai;

import com.github.paicoding.forum.service.ai.dto.AiSummaryReq;
import com.github.paicoding.forum.service.ai.dto.AiSummaryResp;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AiClient {
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * D41 修改：改为动态配置
     * 默认值：http://localhost:8081/inner/ai/summary (方便本地测试)
     * Docker 环境：通过环境变量 AI_SERVICE_URL 覆盖
     */
    @Value("${ai.service.url:http://localhost:8081/inner/ai/summary}")
    private String aiServiceUrl;

    public String getSummary(String title, String content) {
        try {
            // 1. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            //D43 新增：透传 TraceId
            // 从当前线程的 MDC 中取出 TraceId
            String traceId = MDC.get("traceId");
            if (traceId != null) {
                headers.add("traceId", traceId);
                log.info(">>>>>> Paicoding 发送 TraceId: {}", traceId); // 加上这一行调试！
            } else {
                log.warn(">>>>>> Paicoding MDC 中没有 TraceId!"); // 加上这一行调试！
            }
            // 2. 构建请求体
            AiSummaryReq reqBody = new AiSummaryReq(title, content);
            HttpEntity<AiSummaryReq> requestEntity = new HttpEntity<>(reqBody, headers);

            log.info("正在发送请求到: {}", aiServiceUrl); // 使用新变量

            // 3. 发送请求 (使用配置的 URL)
            AiSummaryResp resp = restTemplate.postForObject(
                    aiServiceUrl, // 使用新变量
                    requestEntity,
                    AiSummaryResp.class
            );

            log.info("收到响应: {}", resp);

            // 4. 处理结果
            if (resp != null && resp.isSuccess()) {
                return resp.getSummary();
            } else {
                log.error("AI 摘要生成失败: {}", resp != null ? resp.getErrorMsg() : "返回为空");
                return null;
            }
        } catch (Exception e) {
            log.error("调用 AI 服务异常", e);
            return null;
        }
    }
}