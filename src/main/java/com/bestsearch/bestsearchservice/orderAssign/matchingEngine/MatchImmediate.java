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
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.swing.text.html.parser.Entity;

@Slf4j
@Component
public class MatchImmediate implements IMatchBehaviour {


  private final OrganizationService organizationService;

  private final OrderAssignmentService orderAssignmentService;

  private final SimpMessagingTemplate simpMessagingTemplate;

  private final OrderAssignmentMapper orderAssignmentMapper;

  private final OrderService orderService;

  private final OrderProducer orderProducer;

  public MatchImmediate(final OrganizationService organizationService,
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
    log.info("Immediate order:", orderOutputDTO.getOrderRef());

    List<OrderAssignment> orderAssignments = getNewAssignments(
        0,
        orderOutputDTO
    );

    orderAssignmentService.saveOrderAssignments(orderAssignments);

    // push everything to web socket queue
    simpMessagingTemplate.convertAndSend("/topic/hello", orderAssignments.stream().map(
        orderAssignmentMapper::toOrderAssignmentDTO
    ));

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
        .findTimeFlyOrders(OrderType.IMMEDIATE);
    if (Objects.isNull(timeFlyOrderAssignments)) {
      return;
    }

    if (Objects.nonNull(timeFlyOrderAssignments) && timeFlyOrderAssignments.size() > 0) {
      Map<Long, Integer> orderIdVsOffset = new HashMap<>();
      timeFlyOrderAssignments.forEach(oa -> {
        oa.setAssignedStatus(Status.NO_RESPONSE);
        orderIdVsOffset.put(oa.getOrderId(),
            oa.getOffsetPaginate()); //all radius shd be same for given orderId
      });

      orderAssignmentService.saveOrderAssignments(timeFlyOrderAssignments);
      simpMessagingTemplate.convertAndSend("/topic/hello"
          , timeFlyOrderAssignments.stream().map(orderAssignmentMapper::toOrderAssignmentDTO));

      List<OrderOutputDTO> orderOutputDTOs = orderService.getOrdersById(orderIdVsOffset.keySet());

      List<OrderAssignmentDTO> toBeSentOrders = new ArrayList<>();
      List<OrderAssignment> toBeSavedAssignments = new ArrayList<>();

      orderOutputDTOs.forEach(orderOutputDTO -> {
        List<OrderAssignment> newAssignments = getNewAssignments(
            orderIdVsOffset.get(orderOutputDTO.getId()), orderOutputDTO);
        if (Objects.nonNull(newAssignments)) {
          toBeSavedAssignments.addAll(newAssignments);
          toBeSentOrders.addAll(
              newAssignments.stream().map(OrderAssignment::viewAsOrderAssignmentDTO)
                  .collect(Collectors.toList()));
        } else {
          //TODO: no pharmacies notify user
        }
      });

      orderAssignmentService.saveOrderAssignments(toBeSavedAssignments);
      simpMessagingTemplate.convertAndSend("/topic/hello", toBeSentOrders);
    }
  }

  private void handleReject(OrderAssignment orderAssignment) {
    List<OrderAssignment> toBeSavedAssignments = new ArrayList<>();
    toBeSavedAssignments.add(orderAssignment);

    List<OrderAssignment> pendingAssignments = orderAssignmentService
        .findByOrderIdAndAssignedStatus(orderAssignment.getOrderId(), Status.PENDING);

    if (Objects.isNull(pendingAssignments) || pendingAssignments.size() < 1) {
      OrderOutputDTO orderOutputDTO = orderService.getOrderById(orderAssignment.getOrderId());
      List<OrderAssignment> newAssignments = getNewAssignments(
          orderAssignment.getOffsetPaginate() + 1,
          orderOutputDTO
      );

      if (!newAssignments.isEmpty()) {
        toBeSavedAssignments.addAll(newAssignments);
        simpMessagingTemplate.convertAndSend("/topic/hello", newAssignments.stream()
            .map(OrderAssignment::viewAsOrderAssignmentDTO)
            .collect(Collectors.toList()));
      } else {
        //TODO: no pharmacies notify user
      }
    }

    orderAssignmentService.saveOrderAssignments(toBeSavedAssignments);
  }

  private void handleAccept(OrderAssignment orderAssignment) {
    List<OrderAssignment> pendingAssignments = orderAssignmentService
        .findByOrderIdAndAssignedStatus(orderAssignment.getOrderId(), Status.PENDING);
    if (Objects.nonNull(pendingAssignments)) {
      List<OrderAssignment> toBeSavedAssignments = new ArrayList<>();
      List<OrderAssignmentDTO> toBeSentAssignments = new ArrayList<>();

      OrderAssignment acceptedAssignment = null;
      for (OrderAssignment pendingAssignment : pendingAssignments) {
        if (orderAssignment.getId().equals(pendingAssignment.getId())) {
          pendingAssignment.setAssignedStatus(Status.ACCEPTED);
          acceptedAssignment = pendingAssignment;
        } else {
          pendingAssignment.setAssignedStatus(Status.CANCELLED_BY_SYSTEM);
          toBeSentAssignments.add(pendingAssignment.viewAsOrderAssignmentDTO());
        }

        toBeSavedAssignments.add(pendingAssignment);
      }

      orderAssignment = acceptedAssignment;

      orderAssignmentService.saveOrderAssignments(toBeSavedAssignments);
      simpMessagingTemplate.convertAndSend("/topic/hello", toBeSentAssignments);
      orderProducer.send(orderAssignment.viewAsOrderAssignmentDTO());
    }
  }


  private List<OrderAssignment> getNewAssignments(int offset, OrderOutputDTO orderOutputDTO) {
    List<OrganizationOutputDTO> organizationOutputDTOs = organizationService
        .getOrderedActiveOrganizationsWithinRadius(
            orderOutputDTO.getLatitude(),
            orderOutputDTO.getLongitude(),
            offset);

    return organizationOutputDTOs.stream()
        .map(org -> OrderAssignment.builder()
            .orderId(orderOutputDTO.getId())
            .organizationId(org.getId())
            .assignedAt(LocalDateTime.now())
            .assignedStatus(Status.PENDING)
            .orderType(OrderType.IMMEDIATE)
            .priority(1)
            .offsetPaginate(offset)
            .build()).collect(Collectors.toList());
  }
}
