package com.bestsearch.bestsearchservice.orderAssign.repository;


import com.bestsearch.bestsearchservice.order.model.Order;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrderAssignmentRepository extends JpaRepository<OrderAssignment,Long> {

  List<OrderAssignment> findByOrderId(Long id);

  @Query(value = "SELECT oa FROM OrderAssignment oa " +
          "WHERE oa.orderId = :id " +
          "and oa.assignedStatus in :statuses ")
  List<OrderAssignment> findByOrderIdAndAssignedStatuses(Long id, List<Status> orderAssignStatuses);

  List<OrderAssignment> findByOrderIdAndAssignedStatus(Long id, Status orderAssignStatus);

  OrderAssignment findByOrderIdAndAssignedStatusAndPriority(long id, Status orderAssignStatus, int priority);

  List<OrderAssignment> findByAssignedStatusAndOrderTypeAndAssignedAtBefore(Status orderAssignStatus, OrderType orderType, LocalDateTime date);

  @Query(value = "SELECT oa FROM OrderAssignment oa " +
          "WHERE oa.organizationId = :organizationId " +
          "and oa.assignedStatus in :statuses " +
          "ORDER BY oa.assignedAt")
  Optional<List<OrderAssignment>> getAssignmentsByStatuses(long organizationId, List<Status> statuses);

  @Modifying
  @Query(value = "UPDATE OrderAssignment oa SET oa.assignedStatus = ?2 WHERE oa.orderId = ?1 AND oa.assignedStatus in (?3)")
  void updateOrderAssignmentByOrderId(long orderId, Status toStatus, List<Status> fromStatues);
}
