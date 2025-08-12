package com.example.k5_iot_springboot.이론;

// == Spring Controller 매핑에서 사용하는 주요 어노테이션 == //

/*
    1. @PathVariable
        : 경로 변수
        : URI 자원 경로 자체에 포함된 변수를 매핑하여 받는 어노테이션
        - 특정 리소스에 접근, 수정, 삭제에 사용

          ex) GET "/books/{isbn}" --> 책들 중 해당 isbn 의 책을 GET 요청

      >> GET, PUT, DELETE 사용 (POST 사용 X)

         1) 리소스를 특정할 수 있는 PK 값을 주로 사용
         2) 경로 내에 {} 로 값을 감싸서 표현
         3) {} 내의 데이터가 @PathVariable 뒤의 매개변수와 매핑 - (@PathVariable Long isbn)

         cf) 옵션
         : 변수명과 파라미터명이 다른 경우  @PathVariable("이름") 따로 명시해야함
            ex ) (@PathVariable("isbn") Long bookId)

    2. @RequestBody
        : 클라이언트의 HTTP 요청 본문(Body)에 담긴 JSON, XML 데이터를
        , 자바 객체로 변환하여 메서드의 파라미터로 매핑할 때 사용
        - JSON, XML 형태를 DTO 객체로 자동 변환해줌 (RequestDto)

         ex)   @PostMapping
             public String createUser(@RequestBody UserCreateRequestDto dto) {
                - 주로 POST, PUT, DELETE 요청에 주로 사용 (GET 사용 X)
              }

        1) 반드시 요청 본문이 존재해야함 (없으면 예외 발생)
        2) 클라이언트는 "Content-Type: application/json" 헤더 설정을 하여 요청해야함
        3) DTO 객체는 반드시 @Getter/@Setter 또는 @Data 필요

        - 복잡한 데이터 전송(객체 구조가 필요한 경우), 민감한 데이터 전송에 사용
            : URL 에 노출되지 않고 Body 에 숨겨 전송이 가능하기 때문

    3. @RequestParam
        : 클라이언트가 보낸 URL 쿼리 스트링 또는 폼 데이터를 메서드 파라미터로 바인딩 할 때 사용
        - "URL 에 노출"되기 때문에 민감하지 않은 데이터에 적합
        - 주로 GET 요청에 사용

        - 간단한 검색 조건, 필터링 & 페이징 기능, 보안이 크게 중요하지 않은 데이터

    cf) 옵션 정리
    @RequestParam(required = true): (기본값) 값이 없으면 오류
    @RequestParam(required = false): 값이 없어도 허용 (null 허용)
    @RequestParam(defaultValue = "값"): 기본값 설정

 */

public class K_Controller {

}
