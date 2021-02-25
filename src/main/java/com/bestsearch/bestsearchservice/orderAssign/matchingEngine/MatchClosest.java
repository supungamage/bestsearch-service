package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;


import com.bestsearch.bestsearchservice.auth.UserAdditionalInfo;
import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
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

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    switch (orderOutputDTO.getStatus()) {
      case COMPLETED:
        this.completeOrder(orderOutputDTO);
      case CANCELLED:
        this.cancelOrder(orderOutputDTO);
      default:
        this.addOrUpdate(orderOutputDTO);
    }
  }

  @Override
  public OrderAssignmentDTO match(OrderAssignmentDTO orderAssignmentDTO) {
    OrderAssignment orderAssignment = orderAssignmentMapper.toOrderAssignment(orderAssignmentDTO);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserAdditionalInfo userAdditionalInfo = (UserAdditionalInfo) authentication.getDetails();
    orderAssignment.setOrganizationId(Long.valueOf(userAdditionalInfo.getInternalId()));
    
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
      timeFlyOrderAssignment.setAssignedStatus(Status.NO_RESPONSE);
      handleReject(timeFlyOrderAssignment);
    });
  }

  private void handleReject(OrderAssignment orderAssignment) {
    List<OrderAssignmentDTO> toBeSentAssignments = new ArrayList<>();
    List<OrderAssignment> toBeSavedAssignments = new ArrayList<>();
    toBeSavedAssignments.add(orderAssignment);
    if (orderAssignment.getAssignedStatus()
        == Status.NO_RESPONSE) { //for time fly orders notify via web socket
      toBeSentAssignments.add(orderAssignment.viewAsOrderAssignmentDTO());
    }
    OrderAssignment nextAssignment = orderAssignmentService
        .findNextAssignment(orderAssignment.getOrderId(), Status.INITIAL,
            orderAssignment.getPriority() + 1);

    if (Objects.nonNull(nextAssignment)) {
      nextAssignment.setAssignedStatus(Status.PENDING);
      nextAssignment.setAssignedAt(LocalDateTime.now());
      toBeSavedAssignments.add(nextAssignment);
      toBeSentAssignments.add(nextAssignment.viewAsOrderAssignmentDTO());
    } else {
      OrderOutputDTO orderOutputDTO = orderService.getOrderById(orderAssignment
          .getOrderId()); //when separate into services consider keep location with assignments rather than service call to order.

      List<OrderAssignment> newAssignments = getNewAssignments(
          orderAssignment.getOffsetPaginate() + 1,
          orderOutputDTO
      );

      if (!newAssignments.isEmpty()) {
        toBeSavedAssignments.addAll(newAssignments);
        toBeSentAssignments.add(newAssignments.get(0).viewAsOrderAssignmentDTO());
      } else {
        //TODO: no pharmacies notify user
      }
    }

    orderAssignmentService.saveOrderAssignments(toBeSavedAssignments);
    simpMessagingTemplate.convertAndSend("/topic/hello", toBeSentAssignments);
  }

  private void handleAccept(OrderAssignment orderAssignment) {
    List<OrderAssignment> initialAssignments = orderAssignmentService
        .findByOrderIdAndAssignedStatus(orderAssignment.getOrderId(), Status.INITIAL);
    if (Objects.nonNull(initialAssignments)) {
      List<OrderAssignment> toBeSavedAssignments = new ArrayList<>();

      OrderAssignment acceptedAssignment = null;
      for (OrderAssignment initialAssignment : initialAssignments) {
        if (!orderAssignment.getId().equals(initialAssignment.getId())) {
          initialAssignment.setAssignedStatus(Status.CANCELLED_BY_SYSTEM);
        } else {
          initialAssignment.setAssignedStatus(Status.ACCEPTED);
          acceptedAssignment = initialAssignment;
        }
        toBeSavedAssignments.add(initialAssignment);
      }

      orderAssignment = acceptedAssignment;

      orderAssignmentService.saveOrderAssignments(toBeSavedAssignments);
      orderProducer.send(orderAssignment.viewAsOrderAssignmentDTO());
    }
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

  private void addOrUpdate(OrderOutputDTO orderOutputDTO) {
    List<OrderAssignmentDTO> orderAssignmentDTOS = orderAssignmentService.findByOrderId(orderOutputDTO.getId());

    if(Objects.isNull(orderAssignmentDTOS) || orderAssignmentDTOS.isEmpty()) {
      this.add(orderOutputDTO);
    } else {
      this.update(orderOutputDTO);
    }
  }

  private void add(OrderOutputDTO orderOutputDTO) {
    log.info("Adding new closest assignments:", orderOutputDTO.getOrderRef());
    List<OrderAssignment> orderAssignments = this.getNewAssignments(
            0, // initial offset
            orderOutputDTO
    );

    orderAssignmentService.saveOrderAssignments(orderAssignments);
    simpMessagingTemplate.convertAndSend("/topic/hello", List.of(
            orderAssignmentMapper.toOrderAssignmentDTO(orderAssignments.get(0))));
  }

  private void update(OrderOutputDTO orderOutputDTO) {
    log.info("Updating closest assignments:", orderOutputDTO.getOrderRef());
    List<OrderAssignment> assignmentsTobeUpdated = orderAssignmentService
            .findByOrderIdAndAssignedStatuses(orderOutputDTO.getId(), List.of(Status.PENDING, Status.INITIAL, Status.ACCEPTED));
    List<OrderAssignment> tobeSentAssignments = new ArrayList<>();
    assignmentsTobeUpdated.forEach(orderAssignment -> {
      orderAssignment.setUserComment(orderOutputDTO.getUserComment());
      if(!orderAssignment.getAssignedStatus().equals(Status.INITIAL)) {
        tobeSentAssignments.add(orderAssignment);
      }
    });

    orderAssignmentService.saveOrderAssignments(assignmentsTobeUpdated);
    simpMessagingTemplate.convertAndSend("/topic/hello", tobeSentAssignments.stream()
            .map(OrderAssignment::viewAsOrderAssignmentDTO));
  }

  private void completeOrder(OrderOutputDTO orderOutputDTO) {
    log.info("Completing closest order by user:", orderOutputDTO.getOrderRef());
    OrderAssignment acceptedAssignment = orderAssignmentService
        .findByOrderIdAndAssignedStatus(orderOutputDTO.getId(), Status.ACCEPTED).get(0);
    acceptedAssignment.setAssignedStatus(Status.COMPLETED);
    orderAssignmentService.saveOrderAssignment(acceptedAssignment.viewAsOrderAssignmentDTO());

    // push to web socket queue
    simpMessagingTemplate.convertAndSend("/topic/hello", List.of(
        orderAssignmentMapper.toOrderAssignmentDTO(acceptedAssignment)));
  }

  private void cancelOrder(OrderOutputDTO orderOutputDTO) {
    log.info("Cancelling closest order by user:", orderOutputDTO.getOrderRef());

    orderAssignmentService
        .updateOrderAssignmentByOrderAndStatus(orderOutputDTO.getId(), Status.CANCELLED_BY_SYSTEM
            , List.of(Status.INITIAL));

    List<OrderAssignment> openOrderAssignments = orderAssignmentService
        .findByOrderIdAndAssignedStatuses(
            orderOutputDTO.getId(), List.of(Status.PENDING, Status.ACCEPTED));

    openOrderAssignments
        .forEach(orderAssignment -> orderAssignment.setAssignedStatus(Status.CANCELLED));
    orderAssignmentService.saveOrderAssignments(openOrderAssignments);

    // push to web socket queue
    simpMessagingTemplate.convertAndSend("/topic/hello",
        openOrderAssignments.stream().map(OrderAssignment::viewAsOrderAssignmentDTO));
  }
}
