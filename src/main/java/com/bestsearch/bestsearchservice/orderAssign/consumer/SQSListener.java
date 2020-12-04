package com.bestsearch.bestsearchservice.orderAssign.consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchClosest;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchImmediate;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchingContext;
import com.bestsearch.bestsearchservice.orderAssign.matchingEngine.MatchingFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import java.util.Objects;

@Slf4j
@Component
@Lazy
public class SQSListener {

  private final MatchingFactory matchingFactory;
  private final ObjectMapper objectmapper;

  public SQSListener(MatchingFactory matchingFactory, final ObjectMapper objectmapper) {
    this.matchingFactory = matchingFactory;
    this.objectmapper = objectmapper;
  }

  @SqsListener(value = "${aws.sqs.order}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
  public void onMessage(String message) throws JsonProcessingException {
    OrderOutputDTO orderOutputDTO = objectmapper.readValue(message, OrderOutputDTO.class);
    log.info("New order received for matching engine", orderOutputDTO.getOrderRef());
    if(Objects.nonNull(orderOutputDTO) && Objects.nonNull(orderOutputDTO.getId())) {
      new MatchingContext(matchingFactory.getMatch(orderOutputDTO.getOrderType())).doMatch(orderOutputDTO);
    }
  }
}
