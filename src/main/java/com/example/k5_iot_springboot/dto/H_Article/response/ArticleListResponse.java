package com.example.k5_iot_springboot.dto.H_Article.response;

import com.example.k5_iot_springboot.entity.H_Article;

import java.time.LocalDateTime;

public record ArticleListResponse(
        Long id,
        String title,
        String authorLoginId,
        LocalDateTime createdAt
) {
    // 도메인 엔티티 -> DTO 매핑 매서드
    public static ArticleListResponse from(H_Article article) {
        return new ArticleListResponse(
                article.getId(),
                article.getTitle(),
                article.getAuthor().getLoginId(),
                article.getCreatedAt()
        );
    }
}
