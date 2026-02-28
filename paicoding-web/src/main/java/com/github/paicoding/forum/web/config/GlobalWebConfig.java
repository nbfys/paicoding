package com.github.paicoding.forum.web.config;

import com.github.paicoding.forum.web.global.TraceIdInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 Web 配置类 (行为配置)
 * 用于注册拦截器、跨域设置等
 */
@Configuration
public class GlobalWebConfig implements WebMvcConfigurer {

    @Autowired
    private TraceIdInterceptor traceIdInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册全链路追踪拦截器，拦截所有请求
        registry.addInterceptor(traceIdInterceptor)
                .addPathPatterns("/**")
                // 排除静态资源，避免无意义的 TraceId 生成
                .excludePathPatterns("/static/**", "/css/**", "/js/**", "/img/**");
    }
}