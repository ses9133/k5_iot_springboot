package com.example.k5_iot_springboot.provider;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

/*
    ==== JwtProvider ===
    : JWT(JSON Web Token) 토큰을 "생성"하고 "검증"하는 역할
    > 로그인 후 서버가 만들어서 클라이언트(브라우저)에게 전달하는 문자열 토큰

    cf) JWT
        : 사용자 정보를 암호화된 토큰으로 저장
        - 클라이언트가 서버에 요청할 때마다 전달(사용자 정보 확인용, Authorization: Bearer <토큰>)
        - 서버는 토큰을 검증하여 누가 요청했는지 판단
        >> 주로 로그인 인증에 사용됨

        +) JWT 구조
            - header: 어떤 알고리즘으로 서명했는지
            - payload: 사용자 정보 (예: username-로그인아이디, roles-권한)
            - signature: 토큰 변조 방지용 서명

        +) HS256 암호화 알고리즘 사용한 JWT 서명
            - 비밀키는 Base64 로 인코딩 지정
            - JWT 만료 기간은 1시간으로 지정
            > 환경 변수 설정 필요(jwt.secret / jwt.expiration) application.properties

        # JwtProvider 전체 역할#
        1) 토큰 생성(발급) - generateJwtToken 메서드
        2) Bearer 제거 - removeBearer 메서드
        3) 토큰 검증/파싱 - parseClaimsInternal 메서드
        4) payload 에 저장되면 데이터 추출(username, roles) - getUsernameFromJwt, getRolesFromJwt 메서드
        5) 만료까지 남은 시간 계산 - getRemainingMillis 메서드

 */
@Component
// cf) @Component(클래스 레벨 선언) - 스프링 런타임시 컴포넌트 스캔을 통해 자동으로 빈을 찾고 등록 (의존성 주입)
//     @Bean(메서드 레벨 선언) - 반환되는 객체를 개발자가 수동으로 빈 등록
public class JwtProvider {

    public static final String BEARER_PREFIX = "Bearer ";
                                            // Bearer 과 토큰사이에는 띄어쓰기 필요
                                            // removeBearer 에서 사용

    /** 커스텀 클레임 키 */
    public static final String CLAIM_ROLES = "roles";

    /** 서명용 비밀키, 엑세스 토큰 만료시간(ms), 만료 직후 허용할 시계 오차(s)*/
    /** application.properites
     * jwt.expiration=3600000
     * jwt.clock-skew-seconds=60
     * */
    // 환경변수에 지정한 비밀키와 만료 시간 저장 변수 선언
    private final SecretKey key;
    private final long jwtExpirationMs;
    private final int clockSkewSeconds;

    // 검증/파싱 담당 파서: 파서를 생성자에서 1회 구성하여 재사용 - 성능/일관성 보장(JJWT의 파서 객체)
    private final JwtParser parser;

    // 생성자: JWTProvider  객체 생성시 비밀키와 만료시간 초기화하는 생성자
    // 환경 변수로 부터 설정 주입 + 파서 준비 시킴
    public JwtProvider(
            // @Value: application.properties 나 application.yml과 같은 설정 파일의 값을 클래스 변수에 주입
            //          >> 데이터 타입 자동 인식
            @Value("${jwt.secret}") String secret, // cf) Base64 인코딩된 비밀키 문자열이어야함
            @Value("${jwt.expiration}") long jwtExpirationMs,    //
            @Value("${jwt.clock-skew-seconds:0}")  int clockSkewSeconds  // 기본 0 - 옵션
    ) {
       // 키 강도 검증(Base64 로 디코딩후 256 비트 이상 권장)
        byte[] secretBytes = Decoders.BASE64.decode(secret);
        if(secretBytes.length < 32) {
            // 32 바이트 == 256 비트
            // : HS256 에 적정한 강도의 키를 강제하여 보안을 강화함
            throw new IllegalArgumentException("jwt.secret은 항상 256비트 이상을 권장합니다.");
        }

        // HMAC-SHA 알고리즘으로 암호화된 키 생성
        this.key = Keys.hmacShaKeyFor(secretBytes); // HMAC-SHA 용 SecretKey 객체 생성
        this.jwtExpirationMs = jwtExpirationMs;
        this.clockSkewSeconds = Math.max(clockSkewSeconds, 0); // 음수 방지

        this.parser = Jwts.parser()
                .verifyWith(this.key) // 해당 키로 서명 검증을 수행하는 파서 (이후 파싱마다 반복 설정 필요X)
                .build();
    }

    /**
     * =================
     *   토큰 생성
     * =================
     */

    /**
     * 액세스 토큰 생성
     * @param username  sub(Subject) 에 저장할 사용자 식별자
     * @param roles     권한 목록(중복 제거용 Set 권장) - JSON 배열로 직렬화 필요(JSON 은 Set 을 인식하지 못하기때문에)
     *
     * subject=sub(username), roles 는 커스텀 클레임 사용 */
    public String generateJwtToken(String username, Set<String> roles) {
        long now = System.currentTimeMillis();
        Date iat = new Date(now);
        Date exp = new Date(now + jwtExpirationMs);

        // List 로 변환하여 직렬화시 타입 안정성 확보
        List<String> roleList = (roles == null) ? List.of() : new ArrayList<>(roles);

        return Jwts.builder()
                // 표준 클레임 sub(Subject) 에 사용자 아이디(또는 고유 식별자) 설정 (claim 의 종류가 subject)
                .setSubject(username)
                .claim(CLAIM_ROLES, roleList) // 커스텀 클레임 키에 권한 목록 저장
                .setIssuedAt(iat) // 표준 클레임에 현재 시간 설정 (발생 시간)
                .setExpiration(exp) // 현재 시간에 만료 시간을 더한 설정(만료 시간)
                //.signWith(key, SignatureAlgorithm.HS256)
                .signWith(key) // 서명 키로 서명(자동 HS256 선택) - 비밀키를 서명
                // key -> 시그니처에 저장
                .compact(); // 빌더를 압축하여 최종 JWT 문자열 생성
    }

    /**
     * ================
     * Bearer 처리
     * ================
     * */
    /** HTTP Authorization 에서 "Bearer" 제거*/
    public String removeBearer(String bearerToken) { // 입력: "Bearer <token>"
        if(bearerToken == null || !bearerToken.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Authorization 형식이 올바르지 않습니다.");
        }
        return bearerToken.substring(BEARER_PREFIX.length()).trim(); // 순수 토큰 반환

}
    /**
     * ================
     * 검증 /파싱
     * ================
     * */
    /**
     * 내부 파싱(검증 포함) - 서명 검증 + 구조 검증한 뒤 Claims(페이로드)를 반환
     *      >> 만료시 clock-skew 허용 옵션
     * */
    private Claims parseClaimsInternal(String token, boolean allowClockSkewOnExpiry) {
       // allowClockSkewOnExpiry: 만료 직후 허용 오차 적용 여부
       try {
           return parser.parseSignedClaims(token).getPayload();
           // 서명 및 기본 구조 검증후 페이로드 (claims)만 추출해 반환
           // 1) 토킁 서명 검증(key로 signature 확인)
           // 2) JWT 기본 구조 검사(header.payload.signature 가 맞는지)
           // 3) 성공시 Claims 꺼내기 가능(.getPayload() 가능)
       } catch (ExpiredJwtException ex) {
           // 토큰이 만료된 경우 발생하는 JJWT 전용 예외 처리
           // : 만료시간이 지난 토큰에도(예외 안에도) Claims 정보는 들어있음
           if(allowClockSkewOnExpiry && clockSkewSeconds > 0 && ex.getClaims() != null) {
               // 호출부가 만료 직후 오차 허용을 활성화했고, 설정값이 0 보다 큰지 확인
               Date exp = ex.getClaims().getExpiration(); // 만료 시각(exp) 추출
               if(exp != null) {
                   long skewMs = clockSkewSeconds * 1000L; // 허용 오차(초)를 밀리초로 변환
                   long now = System.currentTimeMillis();
                   if(now - exp.getTime() <= skewMs) {
                       // 현재 시작 - 만료시각 <= 허용오차 이면 "방금 만료"했다고 간주
                       return ex.getClaims(); // 예외에서 Claims 를 꺼내 그대로 유효한 것으로 반환
                   }
               }
       }
           throw ex; // 허용 오차 범위를 벗어나면 원래의 만료 예외를 다시 던짐
           // ex) 토큰 만료가 12:00:00 이고 서버가 12:00:45로 45초 빠른 경우 clockSkewSeconds 가 60이기 때문에
           //           , 유효한 요청으로 판단하여 Claims 반환할수 있게 한다
    }

}

    /** 토큰 유효성 검사(서명/만료 포함) / clock-skew 허용 적용
     * >> 컨트롤러/필터에서 사용가능한 토큰인지 확인 */
    public boolean isValidToken(String tokenWithoutBearer) {
        try {
            // 검증 : 서명 불일치, 변조, 포맷 이상, 만료(허용 오차 초과) 등 모든 예외는 catch 로 전달 - false 반환
            parseClaimsInternal(tokenWithoutBearer, true);
            // parseSignedClaims(token) 메서드가 검증 오류중 어떤 오류인지 확인해줌
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Claims 추출 (검증 포함) */
    public Claims getClaims(String tokenWithoutBearer) {
        // 유효성 검사 + 파싱을 한 번에 처리하고, payload(Claims) 반환
        return parseClaimsInternal(tokenWithoutBearer, true);
    }

    /** 실제 페이로드 값 추출
     * : sub - .getSubject()
     * : 커스텀 클레임 - .get("클레임명")
     * */
    public String getUsernameFromJwt(String tokenWithoutBearer) {
        return getClaims(tokenWithoutBearer).getSubject();
    }

    /** roles >> Set<String> 변환 */
    @SuppressWarnings("unchecked") // 제네릭 캐스팅 경고 억제 (런타임 타입 확인으로 보완)
    public Set<String> getRolesFromJwt(String tokenWithoutBearer) {
        Object raw = getClaims(tokenWithoutBearer).get(CLAIM_ROLES);
        if(raw == null) return Set.of(); // 권한 없음

        if(raw instanceof List<?> list) {
            Set<String> result = new HashSet<>(); // 중복제거 목적
            for(Object o: list) if(o != null) result.add(o.toString());
            return result;
        }

        if(raw instanceof Set<?> set) {
            Set<String> result = new HashSet<>();
            for(Object o: set) if(o != null) result.add(o.toString());
            return result;
        }


        return Set.of(raw.toString());
    }

    /** 남은 만료 시간(ms)이 음수면 이미 만료*/
    public long getRemainingMillis(String tokenWithoutBearer) {
        Claims c = parseClaimsInternal(tokenWithoutBearer, true);
        return c.getExpiration().getTime() - System.currentTimeMillis();
    }
}