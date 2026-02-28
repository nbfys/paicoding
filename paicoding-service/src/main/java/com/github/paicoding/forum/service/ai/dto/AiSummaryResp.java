package com.github.paicoding.forum.service.ai.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AI摘要生成结果", description = "AI 服务返回的结构")
public class AiSummaryResp {

    @ApiModelProperty(value = "AI生成的摘要文本", example = "这篇文章主要讲了...", required = true)
    private String summary;

    @ApiModelProperty(value = "生成是否成功", example = "true", notes = "true:成功, false:失败")
    private boolean success;

    @ApiModelProperty(value = "错误信息", example = "API Key额度已耗尽", notes = "仅在 success=false 时有值")
    private String errorMsg;
}