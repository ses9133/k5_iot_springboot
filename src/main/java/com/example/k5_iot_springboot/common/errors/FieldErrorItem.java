package com.example.k5_iot_springboot.common.errors;

public record FieldErrorItem (
        String field, // 필드명
        String reject, // 거부값(문자열화)
        String message // 오류 사유
) {}
