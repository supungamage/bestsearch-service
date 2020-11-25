package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;

import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;

public interface IMatchBehaviour {
  void match(OrderOutputDTO orderOutputDTO);
  void match(OrderAssignmentDTO orderAssignmentDTO);
  void match();
}
