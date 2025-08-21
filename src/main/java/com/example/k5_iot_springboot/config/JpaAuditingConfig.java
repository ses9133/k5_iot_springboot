package com.example.k5_iot_springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/*
    JPA Auditing 을 전역 설정
    - @CreatedDate, @LastModifiedDate 등이 동작하려면 필수임
 */
@Configuration // 전역 설정
@EnableJpaAuditing // JPA Auditing 을 사용가능하도록 설정한다
public class JpaAuditingConfig {

}
