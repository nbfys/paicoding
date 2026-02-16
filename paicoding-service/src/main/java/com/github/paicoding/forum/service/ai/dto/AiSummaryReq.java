package com.github.paicoding.forum.service.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiSummaryReq {
    private String title;
    private String content;
}