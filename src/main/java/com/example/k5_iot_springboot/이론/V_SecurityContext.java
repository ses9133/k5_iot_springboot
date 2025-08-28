package com.example.k5_iot_springboot.이론;
/*
    ===== 스프링 시큐리티 동작 흐름 =====

    1. 토큰 생성 (요청 자격 증명)
         #G_AuthServiceImpl
        - new UsernamePasswordAuthenticationToken(req.loginId(), req.password())
        - 상태: authenticated=false, principal=loginId, credentials=rawPassword

    2. AuthenticationManager.authenticate(..) 호출  (G_AuthServiceImpl 클래스)
    - AuthenticationManager의 구현체: ProviderManager
    - 내부에 여러 AuthenticationProvider 리스트가 있고,
        토큰 타입을 처리할 수 있는 Provider 에게 순서대로 위임

    3. DaoAuthenticationProvider가 담당(DaoAuthenticationProvider 는 AuthenticationProvider 리스트 중 하나)
        - 해당 토큰 타입(UsernamePasswordAuthenticationToken)을 지원하므로 처리 진입

    4. 사용자 로딩(UserDetailsService)
        - CustomUserDetailsService.loadUserByUsername(username) 호출
        - userRepository.findByLoginId(username) 로 G_User 엔티티 조회
        - 조회 실패시, UsernameNotFoundException 발생("사용자를 찾을 수 없습니다."로 외부 전달)

    5. 보안 표현으로 매핑
        조회된 G_User 엔티티 -> UserPrincipalMapper.map(user) 로 전달하여
        -> UserPrincipal 생성: id, username, password(해시값), authorities(ROLE_*), 계정 상태 플래그

    6. 비밀번호 검증(PasswordEncoder)
        - PasswordEncoder.matches(raw, encoded)
            >> 불일치: BadCredentialException
            >> 일치: 다음 단계 진행

    7. 계정 상태 점검
        - isAccountNonLocked, isEnabled, isAccountNonExpired, isCredentialsNonExpired
        - 하나라도 false 면 각각의 예외 발생(잠금 ,비활성, 만료 등...)

    8. 인증 성공 토큰 생성
        - 새로운 UsernamePasswordAuthenticationToken(principal, null, authorities) (JwtAuthenticationFilter - setAuthenticationContext)
        - 상태: authenticated=true, principal=UserPrincipal, credential=null(보안상 삭제), authorities 포함

   9. AuthenticationManager 가 성공토큰을 반환
        - Authentication auth 가 돌아옴 (ServiceImpl)
        - auth.getPrincipal() -> UserPrincipal 반환
        - auth.getAuthorities() -> ROLE_* 목록

    10. JWT 발급(애플리케이션 레벨)
        - auth 정보(사용자 PK, username, roles 등) 로 Access Token 생성
        - 클레임: sub=username, roles, iat, exp

    11. 응답 작성
        - 헤더 Authorization: Bearer <토큰> 또는 바디 JSON 변환
        - 이후 클라이언트 요청마다 해당 토큰을 Authorization 헤더로 전송

------------------------
    +) 이후 요청 처리(인가 단계)
        - JWT 필터가 헤더의 토큰 검증 >> 유효하면 UserPrincipal 재구성 >> SecurityContext 에 주입

 */
public class V_SecurityContext {

}
