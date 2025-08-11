package com.example.k5_iot_springboot.이론;

/*
    === HTTP (HyperText Transfer protocol) ===

    1. 인터넷의 기원
        : 1960~1970 년대 미국에서 시작
        - 군사용 목적, 컴퓨터 간 통신을 위해 개발
        - 여러 컴퓨터를 네트워크로 연결하는 "아르파넷" 이라는 프로젝트가 시작
            >> 그게 지금의 "인터넷"으로 발전

    2. HTTP 통신의 기원
        : 1980 년도 후반 ~ 1990 년도 초반
        - 인터넷 연결 이후, 정보 요청과 요청에 대한 응답의 규칙을 생성
        - HyperText Transfer(전송) Protocol(규약)

    3. HTTP 통신이란 ?
        1) HyperText: 단순한 글자가 아니라, 링크를 통해 다른 문서로 넘어갈 수 있는 '문서'
        2) Transfer Protocol: 주고 받는 약속(규칙)

        >> 하이퍼 텍스트 문서를 인터넷에서 주고 받는 방법을 명시한 규칙

        - 요청을 보내는 측: 클라이언트
        - 응답을 생성하고 보내는 측: 서버

    4. 요청과 응답의 구성
        1) Request
            : 클라이언트가 서버에게 전달
            <Request 의 구조>
            - URL: 어디에 요청할 지
            - HTTP 메서드(GET(조회), POST(등록), PUT(수정), DELETE) : 어떤 종류의 요청인지
            - 헤더(Header): 추가 정보 (클라이언트 정보, 응답 데이터에 대한 형식 등)
            - 바디(Body): 데이터 명시 (요청 시 전달할 데이터 - 주로 POST, PUT 에서 사용)

        2) Response
            : 서버가 클라이언트에게 전달
            <Response 의 구조>
            - Status code: 어떻게 되었는지 (200 OK, 404 NOT FOUND 등)
            - Header: 추가 정보 (응답 데이터 형식 등)
            - Body: 실제 내용(HTML 파일, JSON 데이터 등)
 */

public class A_HTTP01 {
}
