package com.example.k5_iot_springboot.이론;

/*
    === URI VS URL ===
    1. URI (Uniform Resource Identifier)
    : 웹의 자원을 식별하는 이름표 (문자열)
    - 웹 페이지, 이미지, 파일, 서비스 "엔드 포인트"

    2. URL (Uniform Resource Locator)
    : 그 자원이 어떻게/어디로 가서 접근하는지 알려주는 주소 + 방법 체계
    > 자원의 위치를 나타내는 문자열, 웹 주소를 의미

    cf) URL 은 URI의 한 종류이다. (URI 가 더 포괄적인 개념)

    https://biz.heraldcorp.com/mnews/article/10551653?sort=asc
    >> URL
        - https: scheme (프로토콜, 접근 방법)
        - biz.heraldcorp.com: 호스트+포트(ex. localhost:8080 - 어느 컴퓨터인지, 어떤 서버인지)
        - mnews/article: path (자원을 나타냄)
        - sort=asc: 쿼리(추가 조건)

    === @RequestMapping 은 URI 자원(path)을 명시 ===
    : 해당 요청으로 어떻나 자원에 접근할 것 인지 작성

    === HTTP 메서드의 @RequestMapping ===
    @RequestMapping("/test") - 클래스와의 연결
    : http://localhost:8080/test 요청

    -- HTTP 메서드 -- (@RequestMapping(" ") 이후로 쌓임)
    @PostMapping

    @GetMapping ("/all")
    : http://localhost:8080/test/all

    @GetMapping ("/{id}")
    : http://localhost:8080/test/1
    - 경로의 id 값을 사용하여 데이터를 구분

    @PutMapping

    @DeleteMapping("/{id}")
 */
public class J_URI {
}
