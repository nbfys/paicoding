package com.github.paicoding.forum.service.ai.dto;

import lombok.Data;

@Data
public class AiSummaryResp {
    private String summary;
    private boolean success;
    private String errorMsg;
}