package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;

import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.orderAssign.mapper.OrderAssignmentMapper;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignStatus;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;
import com.bestsearch.bestsearchservice.orderAssign.service.OrderAssignmentService;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationOutputDTO;
import com.bestsearch.bestsearchservice.organization.service.OrganizationService;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class MatchImmediate implements IMatchBehaviour{


  private final OrganizationService organizationService;

  private final OrderAssignmentService orderAssignmentService;

  private final SimpMessagingTemplate simpMessagingTemplate;

  private final OrderAssignmentMapper orderAssignmentMapper;

  public MatchImmediate(final OrganizationService organizationService,
                        final OrderAssignmentService orderAssignmentService,
                        final SimpMessagingTemplate simpMessagingTemplate,
                        final OrderAssignmentMapper orderAssignmentMapper) {
    this.organizationService = organizationService;
    this.orderAssignmentService = orderAssignmentService;
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.orderAssignmentMapper = orderAssignmentMapper;
  }

  @Override
  public void match(OrderOutputDTO orderOutputDTO) {
    System.out.println("Immediate...");
    List<OrganizationOutputDTO> organizationOutputDTOs = organizationService.getActiveOrganizationsWithinRadius(
        10.0, // TODO: external variable
        orderOutputDTO.getLatitude(),
        orderOutputDTO.getLongitude());

      List<OrderAssignment> orderAssignments = organizationOutputDTOs.stream().map(
          org -> OrderAssignment.builder()
                  .orderId(orderOutputDTO.getId())
                  .organizationId(org.getId())
                  .assignedDate(LocalDateTime.now())
                  .assignedStatus(OrderAssignStatus.PENDING)
                  .orderType(OrderType.IMMEDIATE)
                  .build()
      ).collect(
          Collectors.toList());

      orderAssignmentService.saveOrderAssignments(orderAssignments);

      // push everything to web socket queue
      simpMessagingTemplate.convertAndSend("/topic/hello"  , orderAssignments.stream().map(
          orderAssignmentMapper::toOrderAssignmentDTO
      ));

  }

  @Override
  public void match(OrderAssignmentDTO orderAssignmentDTO) {
    if(orderAssignmentDTO.getAssignedStatus() == OrderAssignStatus.REJECTED){
      // update order assignment status
      orderAssignmentService.saveOrderAssignment(orderAssignmentDTO);
    } else if (orderAssignmentDTO.getAssignedStatus() == OrderAssignStatus.ACCEPTED){
      // update order assignment status
      orderAssignmentService.saveOrderAssignment(orderAssignmentDTO);

      // TODO: update order status and assign organization
      // TODO: update order assign as CANCELLED
      // TODO: Push cancelled status to other organizations - WS
    }
  }

  @Override
  public void match() {

  }
}
