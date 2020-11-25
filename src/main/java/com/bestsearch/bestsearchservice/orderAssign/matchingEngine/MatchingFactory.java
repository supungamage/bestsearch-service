package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;

import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import org.springframework.stereotype.Component;

@Component
public class MatchingFactory {

  private final MatchClosest matchClosest;
  private final MatchImmediate matchImmediate;
  private final MatchPreferred matchPreferred;

  public MatchingFactory(final MatchClosest matchClosest,
                         final MatchImmediate matchImmediate,
                         final MatchPreferred matchPreferred) {
    this.matchClosest = matchClosest;
    this.matchImmediate = matchImmediate;
    this.matchPreferred = matchPreferred;
  }

  public IMatchBehaviour getMatch(OrderType orderType){
    switch (orderType){
      case IMMEDIATE: return matchImmediate;
      case PREFERRED: return matchPreferred;
      default: return matchClosest;
    }
  }
}
