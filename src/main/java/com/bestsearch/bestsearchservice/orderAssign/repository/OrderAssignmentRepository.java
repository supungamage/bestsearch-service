package com.bestsearch.bestsearchservice.orderAssign.repository;


import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignStatus;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderAssignmentRepository extends JpaRepository<OrderAssignment,Long> {

  public List<OrderAssignment> findByOrderId(Long id);

  OrderAssignment findByOrderIdAndAssignedStatusAndPriority(long id, OrderAssignStatus orderAssignStatus, int priority);

  List<OrderAssignment> findByAssignedStatusAndOrderTypeAndAssignedDateBefore(OrderAssignStatus orderAssignStatus, OrderType orderType, LocalDateTime date);

}
