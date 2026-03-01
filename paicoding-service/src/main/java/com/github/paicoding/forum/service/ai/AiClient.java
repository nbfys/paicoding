package com.github.paicoding.forum.service.ai;

import com.github.paicoding.forum.service.ai.dto.AiSummaryReq;
import com.github.paicoding.forum.service.ai.dto.AiSummaryResp;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    /**
     * D44 改造：增加熔断保护
     * name = "aiServiceBreaker" 对应 yml 里的实例名
     * fallbackMethod = "getSummaryFallback" 指定降级方法
     */
    @CircuitBreaker(name = "aiServiceBreaker", fallbackMethod = "getSummaryFallback")
    public String getSummary(String title, String content) {
        // 1. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        // D43 新增：透传 TraceId
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            headers.add("traceId", traceId);
            log.info(">>>>>> Paicoding 发送 TraceId: {}", traceId);
        } else {
            log.warn(">>>>>> Paicoding MDC 中没有 TraceId!");
        }

        // 2. 构建请求体
        AiSummaryReq reqBody = new AiSummaryReq(title, content);
        HttpEntity<AiSummaryReq> requestEntity = new HttpEntity<>(reqBody, headers);

        log.info("正在发送请求到: {}", aiServiceUrl);

        // 3. 发送请求（不吞异常，让Resilience4j捕获）
        AiSummaryResp resp = restTemplate.postForObject(
                aiServiceUrl,
                requestEntity,
                AiSummaryResp.class
        );

        log.info("收到响应: {}", resp);

        // 4. 处理结果：返回失败则主动抛异常，触发熔断统计
        if (resp != null && resp.isSuccess()) {
            return resp.getSummary();
        } else {
            String errorMsg = resp != null ? resp.getErrorMsg() : "AI服务返回空响应";
            log.error("AI 摘要生成失败: {}", errorMsg);
            throw new RuntimeException("AI服务返回失败: " + errorMsg);
        }
    }

    /**
     * 熔断降级方法
     * 规则：参数需和原方法完全一致，最后追加一个 Throwable 类型参数接收异常
     */
    public String getSummaryFallback(String title, String content, Throwable t) {
        String traceId = MDC.get("traceId");
        log.error("触发AI服务熔断/降级! 标题: {}, 异常原因: {}, TraceId: {}",
                title, t.getMessage(), traceId);
        // 返回兜底文案，也可根据业务需求返回null或空字符串
        return "AI摘要暂时不可用 (系统繁忙，请稍后再试)";
    }
}