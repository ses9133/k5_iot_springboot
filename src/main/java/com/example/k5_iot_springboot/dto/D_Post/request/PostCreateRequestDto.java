package com.example.k5_iot_springboot.dto.D_Post.request;

// record
// : Java 16에 도입된 새로운 클래스 선언 방식
// - 데이터를 담기 위한 불변 클래스 (Setter 설정 등 X)
// - DTO, VO, 엔티티와 같은 데이터 전달용 클래스 생성시 사용

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// >> 필드, 생성자, getter, equals(), hashCode(), toString() 자동 생성
@JsonIgnoreProperties(ignoreUnknown = true)
// : 클라이언트의 요청값을 역직렬화(JSON -> Entity)할 때 POJO(클래스)에 없는 JSON 필드가 와도 에러 발생시키지 않고 무시함
public record PostCreateRequestDto (
        @NotBlank(message = "제목은 필수 입력값 입니다.")
        @Size(max = 200, message = "제목은 최대 200자 까지 입력 가능합니다.")
        String title,

        @NotBlank(message = "내용은 필수 입력값 입니다.")
        @Size(max = 10_000, message = "내용은 최대 10,000자 까지 입력 가능합니다.")
        String content,

        @NotBlank(message = "작성자는 필수 입력값 입니다.")
        @Size(max = 100, message = "메시지는 최대 100자까지 입력 가능합니다.")
        String author
){}
