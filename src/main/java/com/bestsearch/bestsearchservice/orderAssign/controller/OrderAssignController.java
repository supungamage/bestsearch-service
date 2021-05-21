package com.bestsearch.bestsearchservice.orderAssign.controller;

import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchingContext;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchingFactory;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;
import com.bestsearch.bestsearchservice.orderAssign.service.OrderAssignmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/api/v1/assignments")
public class OrderAssignController {

  private final MatchingFactory matchingFactory;

  private final OrderAssignmentService orderAssignmentService;

  public OrderAssignController(final MatchingFactory matchingFactory,
      final OrderAssignmentService orderAssignmentService) {
    this.matchingFactory = matchingFactory;
    this.orderAssignmentService = orderAssignmentService;
  }

  @MessageMapping("/update")
  public void handlePrivateMessaging(@Payload OrderAssignmentDTO orderAssignmentDTO,
      @DestinationVariable("channelId") String channelId) {
    log.info(
        "Private message handling " + orderAssignmentDTO.getAssignedStatus() + " " + channelId);
    new MatchingContext(matchingFactory.getMatch(orderAssignmentDTO.getOrderType()))
        .doMatch(orderAssignmentDTO);
  }

  @PutMapping("/{id}")
  public ResponseEntity<OrderAssignmentDTO> updateOrderAssignment(
      @PathVariable("id") long id,
      @RequestBody OrderAssignmentDTO orderAssignmentDTO) {
    orderAssignmentDTO.setId(id); // TODO: do we really need to do this way
    return ResponseEntity
        .ok(new MatchingContext(matchingFactory.getMatch(orderAssignmentDTO.getOrderType()))
            .doMatch(orderAssignmentDTO));
  }

  @GetMapping("/current")
  public ResponseEntity<Map<LocalDate, List<OrderAssignmentDTO>>> getCurrentAssignments(
      @RequestParam long organizationId) {
    return ResponseEntity.ok(this.orderAssignmentService.getCurrentAssignments(organizationId));
  }

  @GetMapping("/past")
  public ResponseEntity<Map<LocalDate, List<OrderAssignmentDTO>>> getPastAssignments(
      @RequestParam long organizationId) {
    return ResponseEntity.ok(this.orderAssignmentService.getPastAssignments(organizationId));
  }

}
