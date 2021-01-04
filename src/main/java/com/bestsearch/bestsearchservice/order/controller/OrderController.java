package com.bestsearch.bestsearchservice.order.controller;

import com.bestsearch.bestsearchservice.order.dto.*;
import com.bestsearch.bestsearchservice.order.dto.OrderCreateDTO;
import com.bestsearch.bestsearchservice.order.dto.OrderInputDTO;
import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.service.OrderImageUploadService;
import com.bestsearch.bestsearchservice.order.service.OrderService;
import java.util.ArrayList;

import org.springframework.http.MediaType;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    private final OrderImageUploadService uploadService;

    public OrderController(final OrderService orderService, final OrderImageUploadService uploadService) {
        this.orderService = orderService;
        this.uploadService = uploadService;
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

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
//        String fileName = fileStorageService.storeFile(file);

//        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
//            .path("/downloadFile/")
//            .path(fileName)
//            .toUriString();

//        return new UploadFileResponse(fileName, fileDownloadUri,file.getContentType(), file.getSize());
        return null;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST,  consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<OrderOutputDTO> addOrder(
            @RequestParam("userId") Long userId,
            @RequestParam("orderType") OrderType orderType,
            @RequestParam("organizationTypeId") Long organizationTypeId,
            @RequestParam("longitude") Double longitude,
            @RequestParam("latitude") Double latitude,
            @RequestParam("organizationId") Long organizationId,
            @RequestParam("files") MultipartFile files[]) {

//        return Arrays.asList(files)
//            .stream()
//            .map(this::uploadFile)
//            .collect(Collectors.toList());

        List<String> images = new ArrayList<String>();


        for (MultipartFile multipartFile : files) {
            images.add(this.uploadService.uploadFile(multipartFile));
        }

        return ResponseEntity.ok(this.orderService.saveOrder(OrderCreateDTO.builder()
            .userId(userId)
            .orderType(orderType)
            .organizationTypeId(organizationTypeId)
            .longitude(longitude)
            .latitude(latitude)
            .organizationId(organizationId)
            .images(images)
            .build()));
    }

}
