package com.bestsearch.bestsearchservice.orderAssign.repository;


import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrderAssignmentRepository extends JpaRepository<OrderAssignment,Long> {

  List<OrderAssignment> findByOrderId(Long id);

  List<OrderAssignment> findByOrderIdAndAssignedStatusNot(Long id, Status orderAssignStatus);

  List<OrderAssignment> findByOrderIdAndAssignedStatus(Long id, Status orderAssignStatus);

  OrderAssignment findByOrderIdAndAssignedStatusAndPriority(long id, Status orderAssignStatus, int priority);

  List<OrderAssignment> findByAssignedStatusAndOrderTypeAndAssignedDateBefore(Status orderAssignStatus, OrderType orderType, LocalDateTime date);
}
