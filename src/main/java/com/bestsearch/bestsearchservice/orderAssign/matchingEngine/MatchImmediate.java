package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;

import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.orderAssign.mapper.OrderAssignmentMapper;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;
import com.bestsearch.bestsearchservice.orderAssign.service.OrderAssignmentService;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationOutputDTO;
import com.bestsearch.bestsearchservice.organization.service.OrganizationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Entity;

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
                  .assignedStatus(Status.PENDING)
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
    OrderAssignment orderAssignment = orderAssignmentMapper.toOrderAssignment(orderAssignmentDTO);
    if(orderAssignmentDTO.getAssignedStatus() == Status.REJECTED){
      handleReject(orderAssignment);
    } else if (orderAssignmentDTO.getAssignedStatus() == Status.ACCEPTED){
      handleAccept(orderAssignment);
    }
  }

  @Override
  public void match() {
    List<OrderAssignment> timeFlyOrderAssignments = orderAssignmentService.findTimeFlyOrders(OrderType.IMMEDIATE);
    if(Objects.isNull(timeFlyOrderAssignments)) {
      return;
    }

    timeFlyOrderAssignments.forEach(oa -> {
      oa.setAssignedStatus(Status.CANCELLED);
    });
    orderAssignmentService.saveOrderAssignments(timeFlyOrderAssignments);
    // push everything to web socket queue
    simpMessagingTemplate.convertAndSend("/topic/hello"  , timeFlyOrderAssignments.stream()
            .map(orderAssignmentMapper::toOrderAssignmentDTO));

    Map<Long, Double> orderIdVsDistance = timeFlyOrderAssignments.stream() //TODO: get distinct for better performance
            .collect(Collectors.toMap(OrderAssignment::getOrderId, OrderAssignment::getRadius));
    List<OrderAssignmentDTO> toBeSentOrders = new ArrayList<>();
    List<OrderAssignment> toBeSavedAssignments = new ArrayList<>();
    orderIdVsDistance.forEach((id, distance) -> {
      //TODO: search again, shd be PENDING all
      List<OrderAssignment> newAssignments = new ArrayList<>();
      if(Objects.nonNull(newAssignments)) {
        toBeSavedAssignments.addAll(newAssignments);
        toBeSentOrders.addAll(newAssignments.stream().map(OrderAssignment::viewAsOrderAssignmentDTO).collect(Collectors.toList()));
      } else {
        //TODO: no pharmacies notify user
      }

    });
    orderAssignmentService.saveOrderAssignments(toBeSavedAssignments);
    //TODO: send to be sent orders
  }

  private void handleReject(OrderAssignment orderAssignment) {
    List<OrderAssignmentDTO> toBeSentOrders = new ArrayList<>();
    List<OrderAssignment> toBeSavedAssignments = new ArrayList<>();
    toBeSavedAssignments.add(orderAssignment);
    toBeSentOrders.add(orderAssignment.viewAsOrderAssignmentDTO());

    List<OrderAssignment> pendingAssignments = orderAssignmentService.findByOrderIdAndAssignedStatus(orderAssignment.getOrderId(), Status.PENDING);

    if(Objects.isNull(pendingAssignments) || pendingAssignments.size() < 1) {
      //TODO: search again, shd be PENDING all
      List<OrderAssignment> newAssignments = new ArrayList<>();
      if(Objects.nonNull(newAssignments)) {
        toBeSavedAssignments.addAll(newAssignments);
        toBeSentOrders.addAll(newAssignments.stream().map(OrderAssignment::viewAsOrderAssignmentDTO).collect(Collectors.toList()));
      } else {
        //TODO: no pharmacies notify user
      }
    }

    orderAssignmentService.saveOrderAssignments(toBeSavedAssignments);
    //TODO: send to be sent orders
  }

  private void handleAccept(OrderAssignment orderAssignment) {
    List<OrderAssignment> toBeSavedAssignments = new ArrayList<>();
    List<OrderAssignmentDTO> toBeSentOrders = new ArrayList<>();

    toBeSavedAssignments.add(orderAssignment);
    toBeSentOrders.add(orderAssignment.viewAsOrderAssignmentDTO());

    List<OrderAssignment> pendingAssignments = orderAssignmentService.findByOrderIdAndAssignedStatus(orderAssignment.getOrderId(), Status.PENDING);
    if (Objects.nonNull(pendingAssignments)) {
      pendingAssignments.forEach(pendingAssignment -> {
        pendingAssignment.setAssignedStatus(Status.CANCELLED);
        toBeSavedAssignments.add(pendingAssignment);
        toBeSentOrders.add(pendingAssignment.viewAsOrderAssignmentDTO());
      });
    }

    orderAssignmentService.saveOrderAssignments(toBeSavedAssignments);
    //TODO: send to be sent orders
  }
}
