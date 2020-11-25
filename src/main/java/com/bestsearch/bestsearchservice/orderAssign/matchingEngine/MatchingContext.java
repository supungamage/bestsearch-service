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

  public void doMatch(OrderAssignmentDTO orderAssignmentDTO){
    this.matchBehaviour.match(orderAssignmentDTO);
  }

  public void doMatch(){
    this.matchBehaviour.match();
  }
}
