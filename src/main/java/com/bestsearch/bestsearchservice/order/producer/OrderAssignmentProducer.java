package com.bestsearch.bestsearchservice.order.producer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderAssignmentProducer {
    private final AmazonSQS amazonSQS;
    private final String queue;
    private final ObjectMapper objectmapper;

    public OrderAssignmentProducer(final AmazonSQS amazonSQS, final ObjectMapper objectmapper,
                              final @Value("${aws.sqs.order}") String queue) {
        this.amazonSQS = amazonSQS;
        this.objectmapper = objectmapper;
        this.queue = queue;
    }

    public void send(OrderOutputDTO orderOutputDTO) {
        log.info("Sending order for assignments...");
        try {
            this.amazonSQS.sendMessage(new SendMessageRequest(queue, objectmapper.writeValueAsString(orderOutputDTO)));
        } catch (JsonProcessingException e) {
            log.error("Error occurred while sending Order to assignment Queue");
        }
    }
}
