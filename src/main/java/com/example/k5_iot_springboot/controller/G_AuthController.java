package com.example.k5_iot_springboot.controller;

import com.example.k5_iot_springboot.dto.G_Auth.request.SignInRequest;
import com.example.k5_iot_springboot.dto.G_Auth.request.SignUpRequest;
import com.example.k5_iot_springboot.dto.G_Auth.response.SignInResponse;
import com.example.k5_iot_springboot.dto.J_Mail.MailRequest;
import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.service.G_AuthService;
import com.example.k5_iot_springboot.service.J_MailService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth") // WebSecurityConfig > /api/v1/auth/** -> 토큰없이 처리 가능
// 회원가입, 로그인, 아이디 찾기, 비밀번호 재설정 등
@RequiredArgsConstructor
public class G_AuthController {
    private final G_AuthService authService;
    private final J_MailService mailService;

    /** 회원가입 */
    @PostMapping("/sign-up")
    public ResponseEntity<ResponseDto<Void>> signUp(@Valid @RequestBody SignUpRequest req) {
        authService.signUp(req);
        return ResponseEntity.ok(ResponseDto.setSuccess("회원가입이 완료되었습니다.", null));
    }

    /** 로그인: AccessToken + RefreshToken 발급 */
    @PostMapping("/sign-in")
    public ResponseEntity<ResponseDto<SignInResponse>> signIn(@Valid @RequestBody SignInRequest req, HttpServletResponse response) {
        ResponseDto<SignInResponse> result = authService.sighIn(req, response);
        return ResponseEntity.ok().body(result);
    }

    /** 로그아웃 (RefreshToken 쿠키 삭제) */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // 쿠키 즉시 만료 처리 (서비스로 넘길 필요 X)
        // jakarta.servlet.http.Cookie
        // : 웹 서버가 웹 브라우저에 저장하도록 보내는 정보 조각
        // - 첫 번째 인자(키), 두 번째 인자(값)를 통해 정보를 전달
        // - 자바 서블릿 환경에서 쿠키를 다루기 위한 클래스
        var cookie = new jakarta.servlet.http.Cookie("refreshToken", null); // 이름이 refreshToken 인 토큰이 있으면 null 처리(없애기)
        cookie.setHttpOnly(true);
        // .setHttpOnly(true); - 쿠키 설정 보안 강화 - 쿠키의 JS 접근 여부 설정, Http 통신에서만 가능하도록 (true: 접근 불가)
        // cookie.setSecure(true); - 쿠키 설정 보안 강화 -  https 통신 환경에서만 서버로 전송
        cookie.setPath("/"); // 쿠키의 적용 범위를 특정 경로로 제한
        cookie.setMaxAge(0); // 쿠키 유효 시간 설정 - 0: 즉시 삭제
        response.addCookie(cookie);
        return ResponseEntity.ok(ResponseDto.setSuccess("로그아웃 성공", null));
    }

    /** 이메일 전송 */
    @PostMapping("/send-email")
    public ResponseEntity<ResponseDto<Void>> sendEmail(@Valid @RequestBody MailRequest.SendMail req) {
        mailService.sendEmail(req);
        return ResponseEntity.noContent().build();
    }

    /** 이메일 인증 */
    @GetMapping("/verify")
    public ResponseEntity<ResponseDto<Void>> verifyEmail(@RequestParam String token) {
        mailService.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }

    /** 아이디 찾기 */

    /** 비밀번호 재설정 */
    @PostMapping("/reset-password")
    public ResponseEntity<ResponseDto<Void>> resetPassword(@Valid @RequestBody MailRequest.PasswordReset req) {
        authService.resetPassword(req);
        return ResponseEntity.noContent().build();
    }
}
