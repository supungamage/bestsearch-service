package com.bestsearch.bestsearchservice.orderAssign.producer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderProducer {

  private final AmazonSQS amazonSQS;
  private final String queue;
  private final ObjectMapper objectmapper;

  public OrderProducer(final AmazonSQS amazonSQS, final ObjectMapper objectmapper,
      final @Value("${aws.sqs.order}") String queue) {
    this.amazonSQS = amazonSQS;
    this.objectmapper = objectmapper;
    this.queue = queue;
  }

  public void send(OrderAssignmentDTO orderAssignmentDTO) {
    log.info("Sending order assignment for orders...");
    try {
      this.amazonSQS.sendMessage(
          new SendMessageRequest(queue, objectmapper.writeValueAsString(orderAssignmentDTO)));
    } catch (JsonProcessingException e) {
      log.error("Error occurred while sending Order Assignment to order Queue");
    }
  }
}

