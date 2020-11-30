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
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Entity;

@Slf4j
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
    log.info("Immediate order:", orderOutputDTO.getOrderRef());
    double radius = 10.0; // TODO: external variable
    List<OrganizationOutputDTO> organizationOutputDTOs = organizationService.getActiveOrganizationsWithinRadius(
            radius,
        orderOutputDTO.getLatitude(),
        orderOutputDTO.getLongitude());

      List<OrderAssignment> orderAssignments = organizationOutputDTOs.stream()
              .map(org -> OrderAssignment.builder()
                  .orderId(orderOutputDTO.getId())
                  .organizationId(org.getId())
                  .assignedDate(LocalDateTime.now())
                  .assignedStatus(Status.PENDING)
                  .orderType(OrderType.IMMEDIATE)
                  .priority(1)
                  .radius(radius) //TODO: change when searching again
                  .build()).collect(Collectors.toList());

      orderAssignmentService.saveOrderAssignments(orderAssignments);

      // push everything to web socket queue
      simpMessagingTemplate.convertAndSend("/topic/hello"  , orderAssignments.stream().map(
          orderAssignmentMapper::toOrderAssignmentDTO
      ));

  }

  @Override
  public OrderAssignmentDTO match(OrderAssignmentDTO orderAssignmentDTO) {
    OrderAssignment orderAssignment = orderAssignmentMapper.toOrderAssignment(orderAssignmentDTO);
    if(orderAssignmentDTO.getAssignedStatus() == Status.REJECTED){
      handleReject(orderAssignment);
    } else if (orderAssignmentDTO.getAssignedStatus() == Status.ACCEPTED){
      handleAccept(orderAssignment);
    }

    return orderAssignmentDTO;
  }

  @Override
  public void match() {
    List<OrderAssignment> timeFlyOrderAssignments = orderAssignmentService.findTimeFlyOrders(OrderType.IMMEDIATE);
    if(Objects.isNull(timeFlyOrderAssignments)) {
      return;
    }

    Map<Long, Double> orderIdVsDistance = new HashMap<>();
    timeFlyOrderAssignments.forEach(oa -> {
      oa.setAssignedStatus(Status.CANCELLED);
      orderIdVsDistance.put(oa.getOrderId(), oa.getRadius()); //all radius shd be same for given orderId
    });

    orderAssignmentService.saveOrderAssignments(timeFlyOrderAssignments);
    simpMessagingTemplate.convertAndSend("/topic/hello"
            ,timeFlyOrderAssignments.stream().map(orderAssignmentMapper::toOrderAssignmentDTO));

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
    simpMessagingTemplate.convertAndSend("/topic/hello" ,toBeSentOrders);
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
    simpMessagingTemplate.convertAndSend("/topic/hello" ,toBeSentOrders);
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
    simpMessagingTemplate.convertAndSend("/topic/hello" ,toBeSentOrders);
  }
}
