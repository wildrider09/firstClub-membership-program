package com.firstclub.membership.controller;

import com.firstclub.membership.dto.DtoMapper;
import com.firstclub.membership.dto.request.PlaceOrderRequest;
import com.firstclub.membership.dto.response.OrderResponse;
import com.firstclub.membership.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Records orders. Orders feed the order-count and monthly-spend signals behind
 * tier eligibility, so this endpoint is handy for demoing tier promotion.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request) {
        var order = orderService.placeOrder(request.userId(), request.amount());
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toOrderResponse(order));
    }
}
