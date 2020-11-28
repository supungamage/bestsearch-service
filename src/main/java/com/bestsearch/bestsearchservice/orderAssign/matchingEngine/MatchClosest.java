package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;


import static java.util.stream.Collectors.toList;

import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import com.bestsearch.bestsearchservice.order.service.OrderService;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.orderAssign.mapper.OrderAssignmentMapper;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;
import com.bestsearch.bestsearchservice.orderAssign.service.OrderAssignmentService;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationOutputDTO;
import com.bestsearch.bestsearchservice.organization.service.OrganizationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class MatchClosest implements IMatchBehaviour {

  private final OrganizationService organizationService;

  private final OrderService orderService;

  private final OrderAssignmentService orderAssignmentService;

  private final SimpMessagingTemplate simpMessagingTemplate;

  private final OrderAssignmentMapper orderAssignmentMapper;

  public MatchClosest(final OrganizationService organizationService,
                      final OrderAssignmentService orderAssignmentService,
                      final SimpMessagingTemplate simpMessagingTemplate,
                      final OrderAssignmentMapper orderAssignmentMapper,
                      final OrderService orderService) {
    this.organizationService = organizationService;
    this.orderAssignmentService = orderAssignmentService;
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.orderAssignmentMapper = orderAssignmentMapper;
    this.orderService = orderService;
  }

  @Override
  public void match(OrderOutputDTO orderOutputDTO) {
    System.out.println("Closest...");
    List<OrganizationOutputDTO> organizationOutputDTOs = organizationService.getOrderedActiveOrganizationsWithinRadius(
        10.0,
        orderOutputDTO.getLatitude(),
        orderOutputDTO.getLongitude());

    List<OrderAssignment> orderAssignments = organizationOutputDTOs.stream().map(
        org -> OrderAssignment.builder()
            .orderId(orderOutputDTO.getId())
            .organizationId(org.getId())
            .assignedDate(LocalDateTime.now())
            .assignedStatus(Status.PENDING)
            .orderType(OrderType.CLOSEST)
            .build()
    ).collect(
        Collectors.toList());;

    orderAssignmentService.saveOrderAssignments(orderAssignments);

    // push 1st one to web socket queue
    simpMessagingTemplate.convertAndSend("/topic/hello", List.of(
        orderAssignmentMapper.toOrderAssignmentDTO(orderAssignments.get(0))));
  }

  @Override
  public void match(OrderAssignmentDTO orderAssignmentDTO) {
    if(orderAssignmentDTO.getAssignedStatus() == Status.REJECTED){
      // update order assignment status
      orderAssignmentService.saveOrderAssignment(orderAssignmentDTO);

      // Get next order assignment and send
      List<OrderAssignmentDTO> orderAssignments = orderAssignmentService.findActiveOrdersByOrderId(orderAssignmentDTO.getOrderId());
      simpMessagingTemplate.convertAndSend("/topic/hello", List.of(orderAssignments.get(0)));

      // TODO: If all are rejected, increase the radius and query again


    } else if (orderAssignmentDTO.getAssignedStatus() == Status.ACCEPTED){
      // update order assignment status
      orderAssignmentService.saveOrderAssignment(orderAssignmentDTO);

      orderService.changeOrderStatusAndOrganization(
          orderAssignmentDTO.getOrderId(),
          orderAssignmentDTO.getAssignedStatus(),
          orderAssignmentDTO.getOrganizationId());


      List<OrderAssignmentDTO> orderAssignmentsToCancel = orderAssignmentService.findNonActiveOrdersByOrderId(orderAssignmentDTO.getOrderId());
      List<OrderAssignment> cancelledOrderAssignments = orderAssignmentsToCancel.stream().map(oa -> {
        oa.setAssignedStatus(Status.CANCELLED);
        return oa;
      }).map(orderAssignmentMapper::toOrderAssignment).collect(toList());

      orderAssignmentService.saveOrderAssignments(cancelledOrderAssignments);




    }

  }

  @Override
  public void match() {
    List<OrderAssignment> timeFlyOrderAssignments = orderAssignmentService.findTimeFlyOrders(OrderType.CLOSEST);
    if(Objects.isNull(timeFlyOrderAssignments)) {
      return;
    }

    timeFlyOrderAssignments.forEach(timeFlyOrderAssignment -> {
      List<OrderAssignmentDTO> toBeSentOrders = new ArrayList<>();
      List<OrderAssignment> toBeSavedAssignments = new ArrayList<>();
      timeFlyOrderAssignment.setAssignedStatus(Status.CANCELLED);
      toBeSavedAssignments.add(timeFlyOrderAssignment);
      toBeSentOrders.add(timeFlyOrderAssignment.viewAsOrderAssignmentDTO());
      OrderAssignment nextAssignment = orderAssignmentService.findNextAssignment(timeFlyOrderAssignment.getOrderId(),
          Status.INITIAL, timeFlyOrderAssignment.getPriority() + 1);

      if(Objects.nonNull(nextAssignment)) {
        nextAssignment.setAssignedStatus(Status.PENDING);
        nextAssignment.setAssignedDate(LocalDateTime.now());
        toBeSavedAssignments.add(nextAssignment);
        toBeSentOrders.add(nextAssignment.viewAsOrderAssignmentDTO());
      } else {
        //TODO: search again, priority 1 shd be PENDING and other shd be INITIAL
        List<OrderAssignment> newAssignments = new ArrayList<>();
        if(Objects.nonNull(newAssignments)) {
          toBeSavedAssignments.addAll(newAssignments);
          toBeSentOrders.add(newAssignments.get(0).viewAsOrderAssignmentDTO());
        } else {
          //TODO: no pharmacies notify user
        }
      }

      orderAssignmentService.saveOrderAssignments(toBeSavedAssignments);
    });



  }
}
