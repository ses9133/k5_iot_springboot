package com.example.k5_iot_springboot.security;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * ==== UserPrincipal ====
 * "보안 관점에서 사용자 표현"을 담당하는 값 객체(Value Object, VO)
 * - Spring Security 가 인증/인가 과정에서 인지하는 "최소한의" 사용자 정보 집합 (VO - createdAt 등과 같이 서비스 차원의 데이터는 넣으면 안됨)
 * - 엔티티(G_User) 자체를 지니지 않고, 인증이 필요한 값만 안전하게 전달/보관
 *      >> 결합도 낮춤, 캐시/직렬화 안정성 향상
 *
 *  필요성
 *  1) 시큐리티의 표준 진입점: AuthenticationProvider(DaoAuthenticationProvider) 는 UserDetails 타입을 통해 사용자 정보와 권한을 검사
 *          >> CustomUserDetailsService 내부의 loadUserByUsername() DB 에서 사용자 정보를 읽고 해당 클래스로 감싼 뒤 반환하면, 이후 인증과정이 표준화되어 동작함
 *  2) 경량/안정성 향상: 영속성 엔티티(G_User)를 SecurityContext 에 보관하면
 *                   , 직렬화 문제, 지연 로딩, 순환 참조 등의 문제 발생 가능성 증가함
 *              >> VO 형태의 UserPrincipal 은 인증에 필요한 최소 데이터만 포함하고 있어 안전함
 *   >> 인증 성공시 Authentication(principal)에 들어가 SecurityContextHolder 에 저장
 *          - 컨트롤러 @AuthenticationPrincipal UserPrincipal principal 로 주입받아 사용
 *          - JWT 발급시 클레임으로 id/username/roles 를 넣는 출처로 활용
 *  >> 권한 모델(authorities): GrantedAuthority 집합
 *      ex) new SimpleGrantedAuthority("ROLE_USER")
 *          - 스프링 시큐리티의 hasRole("USER") / hasAuthority("ROLE_USER") 검사와 호환되도록 "ROLE_" 접두어를 붙이는 것을 권장!
 *
 * >> 설계 포인트
 *   1) 불변성: 모든 필드는 final
 *   2) @JsonIgnore, @ToString(exclude="password") 를 통해 비밀번호 유출을 2중 차단
 *   3) 빌더 사용: 가독성 향상, 테스트 용이
 * */
@Getter
@ToString(exclude = "password") // 로그에 password가 노출되지 않도록 하는 과정
public class UserPrincipal implements UserDetails {
    // UserDetails: 시큐리티가 요구하는 사용자 정보 인터페이스
    //              >> 스프링 시큐리티가 사용자의 정보를 불러오기 위해서는 UserDetails 를 구현해야함

    private final Long id;           // PK
    private final String username;  // 로그인 아이디 (서비스에서 이메일 형식으로 바뀔수도있음)
    @JsonIgnore
    private final String password;  // 해시 비밀번호
    private final Collection<? extends GrantedAuthority> authorities; // 권한(ROLE_~)

    /** 계정 상태 플래그들
     *
     * 각 데이터가 false 면 만료 - 로그인 거부
     *
     * EX) 도메인 정책 예시
     *  1. 비밀번호 5회 실패 - accountNonLocked=false
     *  2. 장기 미접속 (휴면상태) - enabled=false
     *  3. 주기적 비밀번호 변경(잦은 변경) - credentialNonExpired=false
     * */
    private final boolean accountNonExpired; // 계정 만료 여부
    private final boolean accountNonLocked; // 계정 잠금 여부
    private final boolean credentialNonExpired; // 비밀번호(자격) 만료 여부
    private final boolean enabled; // 활성화 여부

    // 생성자: 불변 객체 생성을 위한 Builder 기반 생성자
    // - 서비스/어댑터(mapper, UserDetailService) 계층에서 엔티티 정보를 읽고 필요한 정보만 골라 UserPrincipal 로 변환하여 반환
    @Builder
    private UserPrincipal(
            Long id,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            boolean accountNonExpired,
            boolean accountNonLocked,
            boolean credentialNonExpired,
            boolean enabled
    ) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.authorities =authorities;
            this.accountNonExpired = accountNonExpired;
            this.accountNonLocked = accountNonLocked;
            this.credentialNonExpired = credentialNonExpired;
            this.enabled = enabled;
    }

    // ===== UserDetails 구현 =====
    /**
     * 스프링 시큐리티가 AuthenticationProvider 및 AccessDecisionManager 를 통해
     * 인증/인가 수행시 아래의 메서드 사용
     *
     * >> 값 반환 이외의 로직 작성 XX
     * */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    } // : 계정의 권한 목록 리턴

    @Override
    public String getPassword() {
        return password;
    }
    // : 계정의 비밀번호 리턴
    // : 인증 단계에서 DaoAuthenticationProvider 가 비밀번호 매칭에 사용(반드시 해시값!)

    @Override
    public String getUsername() {
        return username;
    } // : 계정의 고유한 값을 리턴
      // : DB PK 값, 중복이 없는 유니크 값

   @Override
   public boolean isAccountNonExpired() {
        return accountNonExpired;
   }
   // : 계정의 만료 여부 리턴

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    // : 계정의 잠김 여부 리턴

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialNonExpired;
    }
    // : 비밀번호 만료 여부 리턴

    @Override
    public boolean isEnabled() {
        return enabled;
    }
    // : 계정의 활성화 여부 리턴
}
