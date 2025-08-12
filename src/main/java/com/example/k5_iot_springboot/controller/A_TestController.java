package com.example.k5_iot_springboot.controller;

import com.example.k5_iot_springboot.entity.A_Test;
import com.example.k5_iot_springboot.service.A_TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @Controller
// : 웹 요청을 처리하는 클래스임을 명시 (반환되는 데이턱 타입이 유연 - JSP, Thymeleaf 등)
// @Controller 는 @Component 를 포함하고 있음

// @ResponseBody
// - 데이터를 반환할 때 HTTP 응답 본문에 직접 출력 / 뷰 리졸버를 거치지 않음 / 데이터 직렬화 수행 (JSON, XML 등)

@RestController
// @RestController
// : @Controller + @ResponseBody

@RequestMapping("/test")
// : 클라이언트로부터의 특정 URI 로 요청이 올 때, 특정 클래스나 특정 메서드와 연결시켜주는 어노테이션 (매핑 담당)
// >> @RequestMapping("URI경로")
public class A_TestController {
    // controller: 요청을 처리 한다는 뜻은 service 층에 전달

    @Autowired // 필드 주입 방식
    A_TestService testService; // testService 에 @Service 어노테이션을 통해서 Bean 등록을 해야 Autowiring 가능해짐

    // 생성
    // @HTTP메서드Mapping("추가URI지정")
    // : 메서드(POST/GET/PUT/DELETE) + localhost:8080/RequestMapping경로/추가URI
    @PostMapping
    public A_Test createTest(@RequestBody A_Test test) {
        A_Test result = testService.createTest(test);
        return result;
    }
    // @RequestBody: HTTP 요청의 바디 부분을 꺼내오는 것
    // 브라우저에서 서버로 요청시, JSON 데이터를 받아오는 역할
    // HTTP 요청의 바디에 담긴 JSON 데이터를 HttpMessageConverter 가 자바객체로 변환해줌

    // 요청 구조 : HTTP 메서드 + URI 경로 (URI 자원에 어떠한 HTTP 동작을 실행할 것인지 명시하여 요청)
    @GetMapping("/all")
    public List<A_Test> getAllTests() {
        List<A_Test> result = testService.getAllTests();
        return result;
    }

    @GetMapping("/{testId}") // 동적인 데이터값은 {}(중괄호)내에 작성, 포스트맨 테스팅할때는 : (콜론으로)
    public A_Test getTestByTestId(@PathVariable Long testId) {
        A_Test result = testService.getTestByTestId(testId);
        return result;
    }

    // ** 메서드가 다르면 경로 달라도 상관없음
    // ** 메서드가 같은데 같은 경로를 쓰면 ->  Ambiguous mapping. Cannot map 'a_TestController' method 에러남

    @PutMapping("/{testId}")
    public A_Test updateTest(@PathVariable Long testId, @RequestBody A_Test test) {
        A_Test result = testService.updateTest(testId, test);
        return result;
    }

    @DeleteMapping("/{testId}")
    public void deleteTest(@PathVariable Long testId) {
        testService.deleteTest(testId);
    }
}

