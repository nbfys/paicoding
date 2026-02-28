package com.github.paicoding.forum.service.ai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "AI摘要生成请求", description = "用于手动触发或重试生成摘要")
public class AiSummaryReq {

    @ApiModelProperty(value = "文章标题", example = "Spring Boot 3.0 新特性全解析", required = true)
    private String title;

    @ApiModelProperty(value = "文章正文内容", example = "本文详细介绍了JDK 17的新语法...", required = true)
    private String content;
}