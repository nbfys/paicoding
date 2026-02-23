# Paicoding AI 助手实战 (D31-D40)

## 1. 项目简介
本项目是在 Paicoding 技术社区的基础上，引入 Spring AI Alibaba 能力，实现 **文章智能摘要** 功能。
通过异步事件驱动架构，实现了 AI 生成与主业务解耦，确保了发布体验的流畅性与系统的鲁棒性。

## 2. 核心功能
- **智能摘要**：发布文章后，自动调用通义千问大模型生成摘要。
- **异步解耦**：基于 Spring Event + @Async，发布操作秒级响应，AI 任务后台执行。
- **故障容错**：引入 Spring Retry，支持自动重试（3次）与失败兜底（状态记录）。
- **无感降级**：当 AI 服务不可用时，前端自动隐藏摘要卡片，不影响页面展示。

## 3. 技术栈
- **Spring Boot 2.7** (主业务) + **Spring Boot 3.3** (AI 服务)
- **Spring AI Alibaba** (接入通义千问)
- **Spring Event** (事件驱动)
- **Spring Retry** (重试机制)
- **MyBatis-Plus** (持久层)
- **Thymeleaf** (前端渲染)

## 4. 架构设计
[ Paicoding (JDK8) ]  --> (HTTP/JSON) --> [ Ai-Service (JDK17) ]
|                                       |
(发布事件)                                (调用大模型)
v                                       v
[ ArticleAiListener ] --> [ AiSummaryService ] --> [ DB: article_ai_summary ]

## 5. 数据库设计
表名：`article_ai_summary`
- `article_id`: 关联文章 ID
- `summary`: AI 生成的摘要内容
- `status`: 0-初始化, 1-成功, 2-失败
- `error_msg`: 错误信息记录