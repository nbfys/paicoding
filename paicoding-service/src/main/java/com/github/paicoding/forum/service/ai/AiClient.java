package com.github.paicoding.forum.service.ai;

import com.github.paicoding.forum.service.ai.dto.AiSummaryReq;
import com.github.paicoding.forum.service.ai.dto.AiSummaryResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AiClient {

    private static final String AI_SERVICE_URL = "http://localhost:8081/inner/ai/summary";

    private final RestTemplate restTemplate = new RestTemplate();

    public String getSummary(String title, String content) {
        try {
            // 1. 设置请求头：强制 Content-Type = application/json
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 某些老版本 Spring 需要加上 Accept
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

            // 2. 构建请求体 (Header + Body)
            AiSummaryReq reqBody = new AiSummaryReq(title, content);
            HttpEntity<AiSummaryReq> requestEntity = new HttpEntity<>(reqBody, headers);

            log.info("正在发送请求到: {}", AI_SERVICE_URL);

            // 3. 发送请求 (使用 postForObject，但传入 HttpEntity)
            AiSummaryResp resp = restTemplate.postForObject(
                    AI_SERVICE_URL,
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