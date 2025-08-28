package com.example.k5_iot_springboot.filter;

import com.example.k5_iot_springboot.entity.G_User;
import com.example.k5_iot_springboot.provider.JwtProvider;
import com.example.k5_iot_springboot.repository.G_UserRepository;
import com.example.k5_iot_springboot.security.UserPrincipal;
import com.example.k5_iot_springboot.security.UserPrincipalMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * ====  JwtAuthenticationFilter =====
 * : JWT 인증 필터
 * - 요청에서 JWT 토큰을 추출
 * >> request 의 header 에서 토큰을 추출하여 검증(유효한 경우 SecurityContext에 인증 정보 저장)
 *
 * cf) Spring Security 가 OncePerRequestFilter를 상속받아 매 요청마다 실행
 * */
@Component // 스프링이 해당 클래스를 관리하도록 지정, 의존성 주입
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization"; // 요청 헤더 키
    private static final String BEARER_PREFIX = JwtProvider.BEARER_PREFIX;

    private final JwtProvider jwtProvider; // 의존성 주입
    private final G_UserRepository g_UserRepository;
    private final UserPrincipalMapper principalMapper;

    /**
     * 스프링 시큐리티 필터가 매 요청마다 호출하는 핵심 메서드
     *    >> OncePerRequestFilter 내 추상메서드 (반드시 구현)
     *
     * @param request 현재 HTTP 요청 객체
     * @param response 현재 HTTP 요청 응답
     * @param filterChain 다음 필터로 넘기기 위한 체인
     * */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // 0) 사전 스킵 조건: 이미 인증된 컨텍스트가 있으면 스킵(그대로 진행) (다른 필터가 인증처리를 한 경우, 중복 인증 방지)
            if(SecurityContextHolder.getContext().getAuthentication() != null) {
                // 현재 스레드(요청) 컨텍스트에 이미 인증 정보가 들어있는지 확인
                // - 다른 필터가 먼저 인증을 끝낸 경우 굳이 중복 인증 X -> 다음으로 진행
                filterChain.doFilter(request, response);
                return;
            }
            // 1) Preflight(OPTIONS, 사전요청) 는 통과(CORS 의 사전 요청)
            // cf) OPTIONS 메서드 - 특정 리소스(URL)에 통신 옵션 정보를 요청하는 데 사용
            if(HttpMethod.OPTIONS.matches(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2) Authorization 헤더에서 JWT 토큰 추출
            String authorization = request.getHeader(AUTH_HEADER);

            // 3) 헤더가 없으면 (= 비로그인 요청) 그냥 통과 - 보호 리소스는 뒤에서 401/403 처리
            if(authorization == null || authorization.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4) 헤더가 있는 경우
            // 4-1) "Bearer " 접두사가 없으면 형식 오류 - 401 즉시 응답
            if(!authorization.startsWith(BEARER_PREFIX)) {
                unauthorized(response, "Authorization 헤더는 'Bearer <token>' 형식이어야합니다.");
                return;
            }

            // 4-2) 헤더에서 토큰을 파싱하여 가져옴("Bearer " 제거하고 순수 토큰 가져오기)
            String token = jwtProvider.removeBearer(authorization);
            if(token.isBlank()) {
                unauthorized(response, "토큰이 비어있습니다.");
                return;
            }

            // 5) 토큰 유효성 검사(서명/만료 포함)
            if(!jwtProvider.isValidToken(token)) {
                unauthorized(response, "토큰이 유효하지 않거나 만료되었습니다.");
                return;
            }

            // 6) 사용자 식별자 & 권한 추출
            String username= jwtProvider.getUsernameFromJwt(token);

            // +) DB 재조회 - UserPrincipal 구성 (최신 권한/상태 반영)
            G_User user = g_UserRepository.findByLoginId(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

            //Set<String> roles = jwtProvider.getRolesFromJwt(token);

            // 7) 권한 문자열 - GrantedAuthority 로 매핑 ("ROLE_" 접두어 보장)
            //  : 스프링 시큐리티가 이해하는 권한 타입으로 변환
            // >> 권한명 앞에 "ROLE_" 접두사가 필요함
           // Collection<? extends GrantedAuthority> authorities = toAuthorities(roles);

            // >> user 데이터에 최신 권한 반영
            UserPrincipal principal = principalMapper.map(user);

            // 8) SecurityContext 에 인증 저장
            //  : 인증 객체를 만들고 SecurityContext 에 저장
            //  : 해당 시점부터 현재 요청은 "username 이라는 사용자가 authorities 권한으로 인증됨" 상태가 됨
            setAuthenticationContext(request, principal);
        } catch (Exception e) {
            logger.warn("JWT filter error", e);
            unauthorized(response, "인증 처리 중 오류가 발생하였습니다.");
            return;
        }
        // 9) 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * SecurityContextHolder 에 인증 객체 세팅
     * */
    private void setAuthenticationContext(HttpServletRequest request, UserPrincipal principal) {

        // 0) 사용자 아이디 (또는 고유 데이터) 를 바탕으로 인증 토큰 생성
        // UsernamePasswordAuthenticationToken 클래스는 스프링 시큐리티에서 자주 쓰이는 인증 토큰 구현체임
        // - 첫번째 인자 Principal (추후 해당 요청에서 파라미터 값으로 해당 값을 자동 추출)
        // - 두번째 인자 Credentials (이미 토큰 검증을 마쳤으므로 null 전달(doFilterInternal 에서 5번 메서드) - 중복 검증 필요 없음)
        // - 세번째 인자 권한 목록
        // >> "username 이라는 사용자가 authorities 권한으로 인증됨" 상태가 됨

        // cf) 권한이 있는 경우(비워지지 않은 경우) - isAuthenticated=true 임
        AbstractAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()); // 비밀번호는 안알려줌 (보안때매)

        // 1) 요청에 대한 세부 정보 설정
        // : 생성된 인증 토큰에 요청의 세부 사항 설정(예: 원격 IP, 세션 ID 등)
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 2) 빈 SecurityContext 객체 생성하고, 인증토큰 주입
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationToken);

        // 3) 시큐리티 컨텍스트 홀더에 생성된 컨텍스트 설정
        //  : 이후 컨트롤러나 서비스에서 SecurityContextHolder.getContext().getAuthentication() 으로, 현재 사용자 정보를 꺼내쓸 수 있음
        SecurityContextHolder.setContext(context);

    }

    /** USER/ADMIN -> "ROLE_USER" / "ROLE_ADMIN" 으로 매핑*/
    public List<GrantedAuthority> toAuthorities(Set<String> roles) {
        if(roles == null || roles.isEmpty()) return List.of();
        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new) // String -> GrantedAuthority 타입으로 매핑(시큐리티가 이해할 수 있는 타입으로)
                .collect(Collectors.toList());

        // cf) "ROLE_" 첨부 이유
        // 스프링 시큐리티 기본 hasRole("권한")은 내부적으로 ROLE_ 가 첨부된 권한 문자열을 찾음
        // - 접두사를 강제해두면 애플리케이션 전반에서 일관성 유지 가능

        // +) hasAuthority("권한") 는 명시된 문자열 그대로 권한을 확인
    }

    /** 401 응답 헬퍼(JSON) */
    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        // HTTP 상태 코드, 문자 인코딩 설정, 응답 본문 형식, JSON 문자열의 응답 본문을 정의/기록
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"result": "fail", 
                 "message": "%s"}
                """.formatted(message));
    }

}
































