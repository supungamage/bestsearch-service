package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;

public class MatchingContext {

  IMatchBehaviour matchBehaviour;

  public MatchingContext(IMatchBehaviour matchBehaviour){
    this.matchBehaviour = matchBehaviour;
  }

  public void doMatch(){
    this.matchBehaviour.match();
  }

}
