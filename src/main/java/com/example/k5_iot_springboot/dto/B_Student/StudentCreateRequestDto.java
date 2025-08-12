package com.example.k5_iot_springboot.dto.B_Student;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter // 필수
@NoArgsConstructor // 필수 - JSON 에서 객체 변환시 기본 생성자가 필요하기 떄문
public class StudentCreateRequestDto {
    private String name;
    private String email;
}

