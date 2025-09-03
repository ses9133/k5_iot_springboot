package com.example.k5_iot_springboot.security.util;

import com.example.k5_iot_springboot.common.enums.OrderStatus;
import com.example.k5_iot_springboot.entity.H_Article;
import com.example.k5_iot_springboot.repository.H_ArticleRepository;
import com.example.k5_iot_springboot.repository.I_OrderRepository;
import com.example.k5_iot_springboot.security.UserPrincipal;
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
    private final I_OrderRepository orderRepository;

    // principal(LoginId) 이 해당 articleId 의 작성자인지 검사
    public boolean isArticleAuthor(Long articleId, Authentication principal) {
       if(principal == null || articleId == null) return  false;
       String loginId = principal.getName();    // JwtAuthenticationFilter 에서 username 으로 주입
        H_Article article = articleRepository.findById(articleId)
                .orElse(null);
        if(article == null) return false;
        return article.getAuthor().getLoginId().equals(loginId);

    }

    // ====== I_Order ======
    // USER 가 본인의 주문 만을 조회/검색할 수 있도록 체크
    public boolean isSelf(Long userId, Authentication authentication) {
        if(userId == null) return false;

        Long me = extractUserId(authentication);

        return userId.equals(me);
    }

    // USER 가 해당 주문을 취소할 수 있는지 확인 (본인 && PENDING 상태여야함)
    public boolean canCancel(Long orderId, Authentication authentication) {
        Long me = extractUserId(authentication);

        return orderRepository.findById(orderId)
                .map(o -> o.getUser().getId().equals(me)
                        && o.getOrderStatus() == OrderStatus.PENDING)
                .orElse(false);

    }

    //=== 프로젝트의 Principal 구조에 맞게 사용자 ID 추출 하는 메서드 === ///
    private Long extractUserId(Authentication authentication) {
        if(authentication == null) return null;

        Object principal = authentication.getPrincipal();

        // 1) 커스텀 principal 사용하는 경우
        if(principal instanceof UserPrincipal up) {
            return up.getId();
        }

        // 2) 다운캐스팅 실패시 - UserPrincipal 의 형태가 아닐때 fallback (근데 거의 안씀)
        return null;
    }

}
