package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;


import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.orderAssign.mapper.OrderAssignmentMapper;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignStatus;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;
import com.bestsearch.bestsearchservice.orderAssign.service.OrderAssignmentService;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationOutputDTO;
import com.bestsearch.bestsearchservice.organization.service.OrganizationService;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class MatchClosest implements IMatchBehaviour {

  OrderOutputDTO orderOutputDTO;

  public MatchClosest(OrderOutputDTO orderOutputDTO){
    this.orderOutputDTO = orderOutputDTO;
  }

  public MatchClosest(){}

  @Autowired
  OrganizationService organizationService;

  @Autowired
  OrderAssignmentService orderAssignmentService;

  @Autowired
  SimpMessagingTemplate simpMessagingTemplate;

  @Autowired
  OrderAssignmentMapper orderAssignmentMapper;

  @Override
  public void match() {
    System.out.println("Closest...");
    List<OrganizationOutputDTO> organizationOutputDTOs = organizationService.getOrderedActiveOrganizationsWithinRadius(
        10.0,
        orderOutputDTO.getLatitude(),
        orderOutputDTO.getLongitude());

    List<OrderAssignment> orderAssignments = organizationOutputDTOs.stream().map(
        org -> OrderAssignment.builder()
            .orderId(orderOutputDTO.getId())
            .organizationId(org.getId())
            .assignedDate(new Date())
            .assignedStatus(OrderAssignStatus.PENDING)
            .orderType(OrderType.CLOSEST)
            .build()
    ).collect(
        Collectors.toList());;

    orderAssignmentService.saveOrderAssignment(orderAssignments);

    // push 1st one to web socket queue
    simpMessagingTemplate.convertAndSend("/topic/", List.of(
        orderAssignmentMapper.toOrderAssignmentDTO(orderAssignments.get(0))));
  }
}
