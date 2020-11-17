package com.bestsearch.bestsearchservice.orderAssign.api;

import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.orderAssign.service.OrderAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class OrderAssignController {

  @Autowired
  OrderAssignmentService orderAssignmentService;

  @MessageMapping("/update")
  public void handlePrivateMessaging(@Payload OrderAssignmentDTO orderAssignmentDTO, @DestinationVariable("channelId") String channelId) throws Exception {
    System.out.println("Private message handling "+ orderAssignmentDTO.getAssignedStatus() + " " + channelId);
    orderAssignmentService.updateOrderAssignmentStatus(orderAssignmentDTO);
  }

}
