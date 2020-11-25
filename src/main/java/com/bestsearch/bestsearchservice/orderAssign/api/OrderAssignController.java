package com.bestsearch.bestsearchservice.orderAssign.api;

import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchingContext;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchingFactory;
import com.bestsearch.bestsearchservice.orderAssign.service.OrderAssignmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

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
}
