package com.example.k5_iot_springboot.security;

import com.example.k5_iot_springboot.entity.G_User;
import com.example.k5_iot_springboot.repository.G_UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 스프링 스큐리티의 DaoAuthenticationProvider 가 "username" 으로 사용자를 찾을 떄 호출하는
 *  공식 확장 지점(UserDetailsService 인터페이스) 구현체
 *
 *  [ 호출 흐름 ]
 *  1. 사용자 - 로그인 요청(username, password)
 *  2. UsernamePasswordAuthenticationFilter 로 들어감
 *  3. DaoAuthenticationProvider 에게 전달
 * [ 4. ] loadUserByUsername(username) -- 이 클래스 영역 (UserDetailsService 호출시 해당 클래스가 자동 호출됨)
 * 5. UserPrincipal 반환
 * 6. PasswordEncoder 로 password 매칭
 * 7. 인증 성공시 Security Context 에 Authentication 저장, 이후 인가 처리 진행
 * */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final G_UserRepository userRepository; // 데이터 접근 계층(사용자 조회 담당)
    private final UserPrincipalMapper principalMapper; // 변환계층(보안 모델로 변환)

    /**
     * loadUserByUser 메서드
     * : DaoAuthenticationProvider가 username 으로 사용자를 찾을 때 호출하는 메서드
     * */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String loginId = (username == null) ? "" : username.trim();
        if(loginId.isEmpty()) throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);

        // 현재는 loginId를 username 으로 사용하는 정책
        // +) 이메일 로그인 정책시 userRepository.findByEmail(username) 형태로 변경해야함
        G_User user = userRepository.findByLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        // 도메인 엔티티를 보안 VO 객체로 변환하여 반환
        return principalMapper.map(user);
    }

}
