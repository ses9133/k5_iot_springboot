package com.example.k5_iot_springboot.repository;

import com.example.k5_iot_springboot.entity.I_Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface I_StockRepository extends JpaRepository<I_Stock, Long> {
    // ==== 락킹 기법 (Locking) ====
    // 1. 락킹
    // : DB 에서 동시에 여러 트랜잭션이 같은 데이터를 접근할 때 데이터의 정합성(일관성)을 보장하기 위해 사용 하는 방법
    // >> 데이터를 건드리는 동안 데이터의 접근 권한을 제어!

    // 1) 비관적 Lock (PESSIMISTIC)
    //      - 다른 트랜잭션이 반드시 해당 데이터를 건드릴 것이라고 비관적으로 가정하고
    //          , 데이터를 읽거나 수정하기 전에 아예 잠궈버리는 방식
    // [장점]: 충돌 가능성 방지, 데이터 무결성 보장
    // [단점]: 동시성이 떨어짐 (다른 트랜잭션은 대기해야하기 때문에 성능 저하)
    // >> Repository 에서 설정

    // 2) 낙관적 Lock (Optimistic)
    //  - 다른 트랜잭션이 동시에 데이터를 수정하지 않을 것이라고 낙관적으로 가정하고
    //      , 실제 커밋할때 버전 번호(Version)를 비교해 충돌 감지 방지
    //   > 주로 @Version 필드를 엔티티에 두고 JPA 가 관리

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM I_Stock s
        WHERE s.product.id = :productId
""")
    Optional<I_Stock> findByProductIdForUpdate(@Param("productId") Long productId);
    //  @Lock(LockModeType.PESSIMISTIC_WRITE)
    // 비관적 잠금(재고 경합 제어를 위함)
    // : 데이터를 수정하기 전에 해당 데이터에 대한 다른 트랜잭션의 접근을 막아 데이터의 정합성을 보장
    // : 읽기 및 쓰기 모두 막는역할
    // 데이터를 조회해서 수정하기전까지(stock.setQuantity(newQuantity)) 잠금한다. (읽기, 쓰기 모두)

    Optional<I_Stock> findByProductId(Long productId);

}
