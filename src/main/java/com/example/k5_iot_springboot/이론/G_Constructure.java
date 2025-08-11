package com.example.k5_iot_springboot.이론;

/*
    === 스프링 부트 계층 ===

    클라이언트 >> Controller >> Service >> Repository >> DB

    1) Controller (Presentation 계층)
        :  HTTP 요청을 받음
        - 입력은 DTO를 검증 (@Valid)하고 비즈니스 로직은 전부 Service 에 위임
        - 반환은 일관된 응답 포맷으로 감싸서 반환(ResponseEntity<ResponseDto<?>>)

    2) Service (Business 계층)
        : 트랜잭션 경계(@Transaction) 가 잡히는 곳
        - 도메인 규칙(중복 검사, 상태 전이, 권환 확인 등)을 처리
        - Repository 를 통해 DB 에 접근
        +) DTO <-> 엔티티 매핑도 (주로) 발생
        +) createdAt/updatedAt 은 엔티티의 @PrePersist/@PreUpdate 대신 서비스 레벨에서 관리 (주로)

    3) Repository(Persistence 계층)
        : JPA 를 통해 순수한 DB 접근만 담당
        - 쿼리 메서드, QueryDSL 등을 정의하고 비즈니스 로직은 절대 작성 XXX

    4) DB
        : 실제 데이터의 영속화
        - 인덱스 / 제약조건/ 트랜잭션 수준 등은 무결성과 성능 향상을 담당
 */

public class G_Constructure {

}
