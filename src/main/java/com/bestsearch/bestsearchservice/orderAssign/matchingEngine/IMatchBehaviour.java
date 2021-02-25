package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;

import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;

public interface IMatchBehaviour {
  /**
   * Match behaviour when orderOutputDTO receive from order module/service
   * 1. adding new order
   * 2. update order (status change or change user comments)
   *
   * @param orderOutputDTO of order details
   */
  void match(OrderOutputDTO orderOutputDTO);

  /**
   * Match behaviour when orderAssignmentDTO receive from orderAssignment module/service
   * when organization accept or reject order assignment.
   * @param orderAssignmentDTO of assignment updates
   * @return OrderAssignmentDTO of updated OrderAssignmentDTO
   */
  OrderAssignmentDTO match(OrderAssignmentDTO orderAssignmentDTO);


  /**
   * Match behaviour when scheduler TimeFly Orders assignments
   * system automatically assign OrderAssignment to next organization/s
   */
  void match();
}
