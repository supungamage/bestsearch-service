package com.bestsearch.bestsearchservice.orderAssign.matchingEngine.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchClosest;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchImmediate;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchingContext;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchingFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;

public class SQSListener {

  private final QueueMessagingTemplate queueMessagingTemplate;

  @Autowired
  public SQSListener(AmazonSQS amazonSQS){
    this.queueMessagingTemplate = new QueueMessagingTemplate((AmazonSQSAsync) amazonSQS);
  }

  // TODO: Need annotation ?
  public void receiveMessage(){
    OrderOutputDTO orderDTO = this.queueMessagingTemplate.receiveAndConvert("queueName", OrderOutputDTO.class);

//    MatchingContext matchingContext;
//
//    if(orderDTO.getOrderType().equals(OrderType.IMMEDIATE)){
//      matchingContext = new MatchingContext(new MatchImmediate(orderDTO));
//    } else {
//      matchingContext = new MatchingContext((new MatchClosest(orderDTO)));
//    }

    MatchingContext matchingContext = new MatchingContext(new MatchingFactory().getMatch(orderDTO));

    matchingContext.doMatch();
  }



}
