package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;


import static java.util.stream.Collectors.toList;

import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.order.model.Order;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import com.bestsearch.bestsearchservice.order.service.OrderService;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.orderAssign.mapper.OrderAssignmentMapper;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;
import com.bestsearch.bestsearchservice.orderAssign.producer.OrderProducer;
import com.bestsearch.bestsearchservice.orderAssign.service.OrderAssignmentService;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationOutputDTO;
import com.bestsearch.bestsearchservice.organization.service.OrganizationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MatchClosest implements IMatchBehaviour {

  private final OrganizationService organizationService;

  private final OrderService orderService;

  private final OrderAssignmentService orderAssignmentService;

  private final SimpMessagingTemplate simpMessagingTemplate;

  private final OrderAssignmentMapper orderAssignmentMapper;

  private final OrderProducer orderProducer;


  public MatchClosest(final OrganizationService organizationService,
      final OrderAssignmentService orderAssignmentService,
      final SimpMessagingTemplate simpMessagingTemplate,
      final OrderAssignmentMapper orderAssignmentMapper,
      final OrderService orderService,
      final OrderProducer orderProducer) {
    this.organizationService = organizationService;
    this.orderAssignmentService = orderAssignmentService;
    this.simpMessagingTemplate = simpMessagingTemplate;
    this.orderAssignmentMapper = orderAssignmentMapper;
    this.orderService = orderService;
    this.orderProducer = orderProducer;
  }

  @Override
  public void match(OrderOutputDTO orderOutputDTO) {
    log.info("Closest order:", orderOutputDTO.getOrderRef());

    List<OrderAssignment> orderAssignments = getNewAssignments(
        0, // initial offset
        orderOutputDTO
    );

    orderAssignmentService.saveOrderAssignments(orderAssignments);

    // push 1st one to web socket queue
    simpMessagingTemplate.convertAndSend("/topic/hello", List.of(
        orderAssignmentMapper.toOrderAssignmentDTO(orderAssignments.get(0))));
  }

  @Override
  public OrderAssignmentDTO match(OrderAssignmentDTO orderAssignmentDTO) {
    OrderAssignment orderAssignment = orderAssignmentMapper.toOrderAssignment(orderAssignmentDTO);
    if (orderAssignmentDTO.getAssignedStatus() == Status.REJECTED) {
      handleReject(orderAssignment);
    } else if (orderAssignmentDTO.getAssignedStatus() == Status.ACCEPTED) {
      handleAccept(orderAssignment);
    }

    return orderAssignmentDTO;
  }

  @Override
  public void match() {
    List<OrderAssignment> timeFlyOrderAssignments = orderAssignmentService
        .findTimeFlyOrders(OrderType.CLOSEST);
    if (Objects.isNull(timeFlyOrderAssignments)) {
      return;
    }

    timeFlyOrderAssignments.forEach(timeFlyOrderAssignment -> {
      timeFlyOrderAssignment.setAssignedStatus(Status.CANCELLED);
      handleReject(timeFlyOrderAssignment);
    });
  }

  private void handleReject(OrderAssignment orderAssignment) {
    List<OrderAssignmentDTO> toBeSentOrders = new ArrayList<>();
    List<OrderAssignment> toBeSavedAssignments = new ArrayList<>();
    toBeSavedAssignments.add(orderAssignment);
    if (orderAssignment.getAssignedStatus()
        == Status.CANCELLED) { //for time fly orders notify via web socket
      toBeSentOrders.add(orderAssignment.viewAsOrderAssignmentDTO());
    }
    OrderAssignment nextAssignment = orderAssignmentService
        .findNextAssignment(orderAssignment.getOrderId(),
            Status.INITIAL, orderAssignment.getPriority() + 1);

    if (Objects.nonNull(nextAssignment)) {
      nextAssignment.setAssignedStatus(Status.PENDING);
      nextAssignment.setAssignedAt(LocalDateTime.now());
      toBeSavedAssignments.add(nextAssignment);
      toBeSentOrders.add(nextAssignment.viewAsOrderAssignmentDTO());
    } else {
      OrderOutputDTO orderOutputDTO = orderService.getOrderById(orderAssignment.getOrderId());

      List<OrderAssignment> newAssignments = getNewAssignments(
          orderAssignment.getOffsetPaginate() + 1,
          orderOutputDTO
      );

      if (!newAssignments.isEmpty()) {
        toBeSavedAssignments.addAll(newAssignments);
        toBeSentOrders.add(newAssignments.get(0).viewAsOrderAssignmentDTO());
      } else {
        //TODO: no pharmacies notify user
      }
    }

    orderAssignmentService.saveOrderAssignments(toBeSavedAssignments);
    simpMessagingTemplate.convertAndSend("/topic/hello", toBeSentOrders);
  }

  private void handleAccept(OrderAssignment orderAssignment) {
    List<OrderAssignment> toBeSavedAssignments = new ArrayList<>();
    List<OrderAssignmentDTO> toBeSentOrders = new ArrayList<>();

    toBeSavedAssignments.add(orderAssignment);

    List<OrderAssignment> initialAssignments = orderAssignmentService
        .findByOrderIdAndAssignedStatus(orderAssignment.getOrderId(), Status.INITIAL);
    if (Objects.nonNull(initialAssignments)) {
      initialAssignments.forEach(initialAssignment -> {
        initialAssignment.setAssignedStatus(Status.CANCELLED);
        toBeSavedAssignments.add(initialAssignment);
        toBeSentOrders.add(initialAssignment.viewAsOrderAssignmentDTO());
      });
    }

    orderAssignmentService.saveOrderAssignments(toBeSavedAssignments);

    orderProducer.send(orderAssignment.viewAsOrderAssignmentDTO());

    simpMessagingTemplate.convertAndSend("/topic/hello", toBeSentOrders);
  }

  private List<OrderAssignment> getNewAssignments(int offset, OrderOutputDTO orderOutputDTO) {
    List<OrganizationOutputDTO> organizationOutputDTOs = organizationService
        .getOrderedActiveOrganizationsWithinRadius(
            orderOutputDTO.getLatitude(),
            orderOutputDTO.getLongitude(),
            offset);

    int index = 1;
    List<OrderAssignment> newAssignments = new ArrayList<>();
    for (OrganizationOutputDTO org : organizationOutputDTOs) {
      newAssignments.add(OrderAssignment.builder()
          .orderId(orderOutputDTO.getId())
          .organizationId(org.getId())
          .assignedAt(index == 1 ? LocalDateTime.now() : null)
          .assignedStatus(index == 1 ? Status.PENDING : Status.INITIAL)
          .orderType(OrderType.CLOSEST)
          .priority(index)
          .offsetPaginate(offset)
          .build());
      index++;
    }

    return newAssignments;
  }
}
