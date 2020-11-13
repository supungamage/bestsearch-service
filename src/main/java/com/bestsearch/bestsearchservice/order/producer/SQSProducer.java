package com.bestsearch.bestsearchservice.order.producer;

import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class SQSProducer {

    private QueueMessagingTemplate queueMessagingTemplate;

    public SQSProducer(QueueMessagingTemplate queueMessagingTemplate) {
        this.queueMessagingTemplate = queueMessagingTemplate;
    }

    public void produce(String queueName, Object message) {
        queueMessagingTemplate.convertAndSend(queueName, message);
    }

}
