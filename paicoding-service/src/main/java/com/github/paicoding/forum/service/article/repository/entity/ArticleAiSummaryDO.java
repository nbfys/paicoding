package com.github.paicoding.forum.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("article_ai_summary")
public class ArticleAiSummaryDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的文章ID
     */
    private Long articleId;

    /**
     * AI生成的摘要内容
     */
    private String summary;

    private Date createTime;

    private Date updateTime;
    /**
     * 状态: 0-初始化, 1-成功, 2-失败
     */
    private Integer status;

    private String errorMsg;
}