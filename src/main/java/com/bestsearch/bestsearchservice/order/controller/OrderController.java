package com.bestsearch.bestsearchservice.order.controller;

import com.bestsearch.bestsearchservice.order.dto.*;
import com.bestsearch.bestsearchservice.order.service.OrderService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(final OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderOutputDTO> addOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        return ResponseEntity.ok(this.orderService.saveOrder(orderCreateDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderOutputDTO> updateOrder(
            @PathVariable("id") long id,
            @RequestBody OrderInputDTO orderInputDTO) {
        return ResponseEntity.ok(this.orderService.updateOrder(id, orderInputDTO));
    }

    @GetMapping
    public ResponseEntity<List<OrderOutputDTO>> getOrders() {
        return ResponseEntity.ok(this.orderService.getOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderOutputDTO> getOrderById(@PathVariable("id") long id) {
        return ResponseEntity.ok(this.orderService.getOrderById(id));
    }

    @GetMapping("/byRef")
    public ResponseEntity<OrderOutputDTO> getOrderByRef(
            @RequestParam String orderRef,
            @RequestParam long organizationTypeId) {
        return ResponseEntity.ok(this.orderService.getOrderByRef(orderRef, organizationTypeId));
    }

    @JsonView(OrderOutputViews.Public.class)
    @GetMapping("/current")
    public ResponseEntity<List<OrderAndPeriodDTO>> getCurrentOrders(
            @RequestParam long orgTypeId,
            @RequestParam long userId) {
        return ResponseEntity.ok(this.orderService.getCurrentOrders(orgTypeId, userId));
    }

    @JsonView(OrderOutputViews.Public.class)
    @GetMapping("/past")
    public ResponseEntity<List<OrderAndPeriodDTO>> getPastOrders(
            @RequestParam long orgTypeId,
            @RequestParam long userId) {
        return ResponseEntity.ok(this.orderService.getPastOrders(orgTypeId, userId));
    }

}
