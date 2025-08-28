package com.example.k5_iot_springboot.dto.H_Article.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 레코드 내부 필드는 불변성
public record ArticleUpdateRequest(
        @NotBlank @Size(max = 200)
        String title,
        @NotBlank
        String content
) {
}
