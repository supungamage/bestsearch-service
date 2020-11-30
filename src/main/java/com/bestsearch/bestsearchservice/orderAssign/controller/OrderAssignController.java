package com.bestsearch.bestsearchservice.orderAssign.controller;

import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchingContext;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchingFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Controller
public class OrderAssignController {

  private final MatchingFactory matchingFactory;

  public OrderAssignController(final MatchingFactory matchingFactory) {
    this.matchingFactory = matchingFactory;
  }

  @MessageMapping("/update")
  public void handlePrivateMessaging(@Payload OrderAssignmentDTO orderAssignmentDTO,
                                     @DestinationVariable("channelId") String channelId) {
    log.info("Private message handling "+ orderAssignmentDTO.getAssignedStatus() + " " + channelId);
    new MatchingContext(matchingFactory.getMatch(orderAssignmentDTO.getOrderType())).doMatch(orderAssignmentDTO);
  }

  @PutMapping("/{id}")
  public ResponseEntity<OrderAssignmentDTO> updateOrderAssignment(
          @PathVariable("id") long id,
          @RequestBody OrderAssignmentDTO orderAssignmentDTO) {
    return ResponseEntity.ok(new MatchingContext(matchingFactory.getMatch(orderAssignmentDTO.getOrderType())).doMatch(orderAssignmentDTO));
  }
}
