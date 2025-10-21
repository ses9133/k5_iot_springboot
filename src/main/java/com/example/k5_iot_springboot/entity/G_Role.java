package com.example.k5_iot_springboot.entity;

import com.example.k5_iot_springboot.common.enums.RoleType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

/**
 * ==== 권한 코드 엔티티 (roles ) ===
 * : PK (role_name) - Enum/문자열 매핑
 * */
@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class G_Role {
    /** 권한 명(PK) - Enum 을 문자열로 저장 */
    @Id @Enumerated(EnumType.STRING)
    @Column(name = "role_name", length = 30, nullable = false)
    private RoleType name;

//    public String toString() {
//        return "ROLE_" + this.name;
//    }
//
//    @Override
//    public String getAuthority() {
//        return name.toString();
//    }
}
