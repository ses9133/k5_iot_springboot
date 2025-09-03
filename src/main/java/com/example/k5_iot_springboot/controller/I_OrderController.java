package com.example.k5_iot_springboot.controller;

import com.example.k5_iot_springboot.common.enums.OrderStatus;
import com.example.k5_iot_springboot.dto.I_Order.request.OrderRequest;
import com.example.k5_iot_springboot.dto.I_Order.response.OrderResponse;
import com.example.k5_iot_springboot.dto.ResponseDto;
import com.example.k5_iot_springboot.security.UserPrincipal;
import com.example.k5_iot_springboot.service.I_OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
/**
 * 주문 생성/승인/취소 + 검색
 * */
public class I_OrderController {
    private final I_OrderService orderService;

    /** 주문 생성: 인증 주체(로그인한 사용자만)의 userId(토큰 내의) 를 사용 */
    @PostMapping
    // cf) ResponseEntity(HttpStatus 상태 코드, HttpHeaders 요청/응답에 대한 요구사항, HttpBody 응답 본문)
    //     HttpBody 응답 본문 타입을 ResponseDto 로 바꿔서 전달하겠다. (데이터 전송 객체)
    //     ResponseDto >> result(boolean), message(String), data(T) 포함되어있음. 프론트에게 자세히 응답하고 싶다.
    public ResponseEntity<ResponseDto<OrderResponse.Detail>> create (
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody OrderRequest.OrderCreateRequest req
                                 ) {
            ResponseDto<OrderResponse.Detail> response = orderService.create(userPrincipal, req);
            return ResponseEntity.ok().body(response); // 밑에꺼보다 자세하게 응답줄 수 있음.
          //  return ResponseEntity.ok(response);
    }

    /** 주문 승인: ADMIN/MANAGER 만 가능. USER 불가능*/
    @PostMapping("/{orderId}/approve")
    public ResponseEntity<ResponseDto<OrderResponse.Detail>> approve(
            @AuthenticationPrincipal UserPrincipal userPrincipal, // 주문 승인자 정보를 저장(활용)할 경우
            @PathVariable Long orderId
    ) {
        ResponseDto<OrderResponse.Detail> response = orderService.approve(userPrincipal, orderId);
        return ResponseEntity.ok().body(response);
    }

    /** 주문 취소: orderStatus가 PENDING 이어야하고, 취소 요청한 사람이 주문한 사람과 일치하는지(본인인지) 확인애햐함
     * USER (본인 + PENDING) 한정, MANAGER, ADMIN
     * APPROVED 상태여도 MANAGER, ADMIN 은 취소가능하다고 가정
     * */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ResponseDto<OrderResponse.Detail>> cancel(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long orderId
    ) {
        ResponseDto<OrderResponse.Detail> response = orderService.cancel(userPrincipal, orderId);
        return ResponseEntity.ok().body(response);
    }

    // 주문검색: USER(본인꺼만), ADMIN, MANAGER는 전체 사용자꺼 다 조회가능
    @GetMapping
    ResponseEntity<ResponseDto<List<OrderResponse.Detail>>> search(
            @AuthenticationPrincipal UserPrincipal userPrincipal, // 로그인한 사용자 정보
            @RequestParam(required = false) Long userId,          // 검색할 사용자 정보
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime from, // 시작 시간 설정
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime to    // 끝 시간 설정
            ) {
        ResponseDto<List<OrderResponse.Detail>> response = orderService.search(userPrincipal, userId, status, from, to);
        return ResponseEntity.ok().body(response);
    }



}
