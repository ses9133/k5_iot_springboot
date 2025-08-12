package com.example.k5_iot_springboot.repository;

import com.example.k5_iot_springboot.entity.A_Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// === Repository === //
// : DB 에 접근하는 객체 (DAO 형태)
// - DB에 데이터를 읽고 쓰는 CRUD 담당 계층
// - JpaRepository 를 상속 받음 <엔티티타입, PK타입> 형태로 연결할 테이블을 명시

// cf) Entity 는 테이블 자체를 1:1 로 매핑
//      Repository 는 Entity 테이블에 CRUD 작업을 수행

@Repository
public interface A_TestRepository extends JpaRepository<A_Test, Long> {
    // 기본 CRUD 메서드는 내장되어있음

}
