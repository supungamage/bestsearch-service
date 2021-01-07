package com.bestsearch.bestsearchservice.order.consumer;

import com.bestsearch.bestsearchservice.order.service.OrderService;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Lazy
public class OrderListener {

  private final ObjectMapper objectmapper;
  private final OrderService orderService;

  public OrderListener(final ObjectMapper objectmapper,
      final OrderService orderService) {
    this.objectmapper = objectmapper;
    this.orderService = orderService;
  }

  @SqsListener(value = "${aws.sqs.order}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
  public void onMessage(String message) throws JsonProcessingException {
    OrderAssignmentDTO orderAssignmentDTO = objectmapper
        .readValue(message, OrderAssignmentDTO.class);
    log.info("Updated order assignment received for matching engine", orderAssignmentDTO.getId());
    if (Objects.nonNull(orderAssignmentDTO.getId())) {
      orderService.changeOrderStatusAndOrganization(orderAssignmentDTO.getOrderId(),
          orderAssignmentDTO.getAssignedStatus(), orderAssignmentDTO.getOrganizationId());

    }
  }
}
