package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;

import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;

public class MatchingContext {

  private final IMatchBehaviour matchBehaviour;

  public MatchingContext(final IMatchBehaviour matchBehaviour){
    this.matchBehaviour = matchBehaviour;
  }

  public void doMatch(OrderOutputDTO orderOutputDTO){
    this.matchBehaviour.match(orderOutputDTO);
  }

  public OrderAssignmentDTO doMatch(OrderAssignmentDTO orderAssignmentDTO){
    return this.matchBehaviour.match(orderAssignmentDTO);
  }

  public void doMatch(){
    this.matchBehaviour.match();
  }
}
