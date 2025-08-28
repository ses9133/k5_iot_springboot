package com.example.k5_iot_springboot.security.util;

import com.example.k5_iot_springboot.entity.H_Article;
import com.example.k5_iot_springboot.repository.H_ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

// cf) 역할 체크(AuthorizationChecker 굳이 필요 없음) VS 소유자 검사 및 리포지토리 접근 체크
// 1. 역할 체크
//      : @PreAuthorize("hasRole('ADMIN')") 만으로 충분함
// 2. 소유자 검사(게시글 작성자만 수정/삭제 가능) (ROLE 만으로는 부족함)
//      , 리포지토리 접근이 필요한 조건(팀원 여부, 프로젝트 멤버십 있는 사람 접근 가능한 경우 등)이 있다면
//      >> 컨트롤러/서비스에 비즈니스 로직을 섞지 않기 위해 Bean 으로 분리 권장
@Component("authz")
@RequiredArgsConstructor
public class AuthorizationChecker {
    private final H_ArticleRepository articleRepository;

    // principal(LoginId) 이 해당 articleId 의 작성자인지 검사
    public boolean isArticleAuthor(Long articleId, Authentication principal) {
       if(principal == null || articleId == null) return  false;
       String loginId = principal.getName();    // JwtAuthenticationFilter 에서 username 으로 주입
        H_Article article = articleRepository.findById(articleId)
                .orElse(null);
        if(article == null) return false;
        return article.getAuthor().getLoginId().equals(loginId);
    }
}
