package com.bestsearch.bestsearchservice.orderAssign.consumer;

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
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Lazy
public class SQSListener {

  @SqsListener(value = "${aws.sqs.bdeOutput}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
  public void onMessage(OrderOutputDTO orderOutputDTO){
    if(Objects.nonNull(orderOutputDTO) && Objects.nonNull(orderOutputDTO.getId())) {
      new MatchingContext(new MatchingFactory().getMatch(orderOutputDTO)).doMatch();
    }
  }
}
