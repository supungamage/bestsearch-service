package com.bestsearch.bestsearchservice.orderAssign.service;


import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.order.model.Order;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.orderAssign.mapper.OrderAssignmentMapper;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;
import com.bestsearch.bestsearchservice.orderAssign.repository.OrderAssignmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bestsearch.bestsearchservice.share.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderAssignmentService {

  private final OrderAssignmentRepository orderAssignmentRepository;

  private final OrderAssignmentMapper orderAssignmentMapper;

  private final String timeFlyPeriod;

  public OrderAssignmentService(final OrderAssignmentRepository orderAssignmentRepository,
                                final OrderAssignmentMapper orderAssignmentMapper,
                                @Value("${order.assignment.timeFly.hours}") String timeFlyPeriod) {
    this.orderAssignmentRepository = orderAssignmentRepository;
    this.orderAssignmentMapper = orderAssignmentMapper;
    this.timeFlyPeriod = timeFlyPeriod;
  }

  public void saveOrderAssignments(List<OrderAssignment> assignments){
    orderAssignmentRepository.saveAll(assignments);
  }

  public void saveOrderAssignment(OrderAssignmentDTO orderAssignmentDTO) {
    orderAssignmentRepository.save(orderAssignmentMapper.toOrderAssignment(orderAssignmentDTO));
  }

  public List<OrderAssignmentDTO> findByOrderId(long orderId) {
    return orderAssignmentRepository.findByOrderId(orderId).stream()
            .map(OrderAssignment::viewAsOrderAssignmentDTO).collect(Collectors.toList());
  }

  public List<OrderAssignment> findTimeFlyOrders(OrderType orderType) {
    LocalDateTime timeFlyTime = LocalDateTime.now().minusHours(Integer.valueOf(timeFlyPeriod));
    return orderAssignmentRepository.findByAssignedStatusAndOrderTypeAndAssignedAtBefore(Status.PENDING, orderType, timeFlyTime);
  }

  public OrderAssignment findNextAssignment(long orderId, Status orderAssignStatus, int priority) {
    return orderAssignmentRepository.findByOrderIdAndAssignedStatusAndPriority(orderId, orderAssignStatus, priority);
  }

  public List<OrderAssignment> findByOrderIdAndAssignedStatus(long orderId, Status assignedStatus) {
    return orderAssignmentRepository.findByOrderIdAndAssignedStatus(orderId, assignedStatus);
  }

  public List<OrderAssignment> findByOrderIdAndAssignedStatuses(long orderId, List<Status> assignedStatuses) {
    return orderAssignmentRepository.findByOrderIdAndAssignedStatuses(orderId, assignedStatuses);
  }

  public Map<LocalDate, List<OrderAssignmentDTO>> getCurrentAssignments(long organizationId) {
    return orderAssignmentRepository.getAssignmentsByStatuses(organizationId, List.of(Status.PENDING, Status.ACCEPTED))
            .orElseThrow(() -> new ResourceNotFoundException("No data found"))
            .stream().map(OrderAssignment::viewAsOrderAssignmentDTO)
            .collect(Collectors.groupingBy(OrderAssignmentDTO::getAssignedDate));
  }

  public Map<LocalDate, List<OrderAssignmentDTO>> getPastAssignments(long organizationId) {
    return orderAssignmentRepository.getAssignmentsByStatuses(organizationId, List.of(Status.REJECTED, Status.COMPLETED, Status.NO_RESPONSE))
            .orElseThrow(() -> new ResourceNotFoundException("No data found"))
            .stream().map(OrderAssignment::viewAsOrderAssignmentDTO)
            .collect(Collectors.groupingBy(OrderAssignmentDTO::getAssignedDate));
  }

  public OrderAssignmentDTO updateOrderAssignment(long orderAssignmentId, String status) {
    return orderAssignmentRepository
        .save(OrderAssignment.builder().id(orderAssignmentId).assignedStatus(Status.valueOf(status))
            .build()).viewAsOrderAssignmentDTO();
  }

  public void updateOrderAssignmentByOrderAndStatus(long orderId, Status toStatus, List<Status> fromStatues) {
    orderAssignmentRepository.updateOrderAssignmentByOrderId(orderId, toStatus, fromStatues);
  }
}
