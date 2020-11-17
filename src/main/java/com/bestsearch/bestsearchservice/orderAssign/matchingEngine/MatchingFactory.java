package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;

import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;

public class MatchingFactory {

  public IMatchBehaviour getMatch(OrderOutputDTO orderOutputDTO){
    switch (orderOutputDTO.getOrderType()){
      case CLOSEST: return  new MatchClosest(orderOutputDTO);
      case IMMEDIATE: return  new MatchImmediate(orderOutputDTO);
      case PREFERRED: return new MatchImmediate();
    }
    return null;
  }
}
