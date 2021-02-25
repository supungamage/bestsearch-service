package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;

import com.bestsearch.bestsearchservice.auth.UserAdditionalInfo;
import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import com.bestsearch.bestsearchservice.order.service.OrderService;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.orderAssign.mapper.OrderAssignmentMapper;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;
import com.bestsearch.bestsearchservice.orderAssign.producer.OrderProducer;
import com.bestsearch.bestsearchservice.orderAssign.service.OrderAssignmentService;
import com.bestsearch.bestsearchservice.organization.dto.OrganizationOutputDTO;
import com.bestsearch.bestsearchservice.organization.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class MatchPreferred implements IMatchBehaviour {

    private final OrganizationService organizationService;

    private final OrderService orderService;

    private final OrderAssignmentService orderAssignmentService;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final OrderAssignmentMapper orderAssignmentMapper;

    private final OrderProducer orderProducer;

    public MatchPreferred(final OrganizationService organizationService,
                        final OrderAssignmentService orderAssignmentService,
                        final SimpMessagingTemplate simpMessagingTemplate,
                        final OrderAssignmentMapper orderAssignmentMapper,
                        final OrderService orderService,
                        final OrderProducer orderProducer) {
        this.organizationService = organizationService;
        this.orderAssignmentService = orderAssignmentService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.orderAssignmentMapper = orderAssignmentMapper;
        this.orderService = orderService;
        this.orderProducer = orderProducer;
    }

    @Override
    public void match(OrderOutputDTO orderOutputDTO) {
        log.info("Preferred order:", orderOutputDTO.getOrderRef());
        switch (orderOutputDTO.getStatus()) {
            case COMPLETED:
                this.completeOrder(orderOutputDTO);
            case CANCELLED:
                this.cancelOrder(orderOutputDTO);
            default:
                this.addOrUpdate(orderOutputDTO);
        }
    }

    private void addOrUpdate(OrderOutputDTO orderOutputDTO) {
        List<OrderAssignmentDTO> orderAssignmentDTOS = orderAssignmentService.findByOrderId(orderOutputDTO.getId());

        if(Objects.isNull(orderAssignmentDTOS) || orderAssignmentDTOS.isEmpty()) {
            this.add(orderOutputDTO);
        } else {
            this.update(orderOutputDTO);
        }
    }

    private void add(OrderOutputDTO orderOutputDTO) {
        log.info("Adding new preferred assignment:", orderOutputDTO.getOrderRef());
        OrderAssignment orderAssignment = this.getNewAssignment(orderOutputDTO);

        if(Objects.nonNull(orderAssignment)) {
            orderAssignmentService.saveOrderAssignment(orderAssignment);
            simpMessagingTemplate.convertAndSend("/topic/hello", List.of(orderAssignmentMapper.toOrderAssignmentDTO(orderAssignment)));
        }
    }

    private OrderAssignment getNewAssignment(OrderOutputDTO orderOutputDTO) {
        OrganizationOutputDTO organizationOutputDTO = organizationService.getOrganizationById(orderOutputDTO.getOrganizationId());

        return OrderAssignment.builder()
                .orderId(orderOutputDTO.getId())
                .organizationId(organizationOutputDTO.getId())
                .assignedAt(LocalDateTime.now())
                .assignedStatus(Status.PENDING)
                .orderType(OrderType.PREFERRED)
                .priority(1)
                .offsetPaginate(0)
                .build();
    }

    private void update(OrderOutputDTO orderOutputDTO) {
        log.info("Updating preferred assignment:", orderOutputDTO.getOrderRef());
        OrderAssignment assignmentTobeUpdated = orderAssignmentService
                .findByOrderIdAndAssignedStatuses(orderOutputDTO.getId(), List.of(Status.PENDING, Status.ACCEPTED)).get(0);

        assignmentTobeUpdated.setUserComment(orderOutputDTO.getUserComment());

        orderAssignmentService.saveOrderAssignment(assignmentTobeUpdated);
        simpMessagingTemplate.convertAndSend("/topic/hello", List.of(assignmentTobeUpdated.viewAsOrderAssignmentDTO()));
    }

    private void completeOrder(OrderOutputDTO orderOutputDTO) {
        log.info("Completing preferred order by user:", orderOutputDTO.getOrderRef());
        OrderAssignment acceptedAssignment = orderAssignmentService
                .findByOrderIdAndAssignedStatus(orderOutputDTO.getId(), Status.ACCEPTED).get(0);
        acceptedAssignment.setAssignedStatus(Status.COMPLETED);
        orderAssignmentService.saveOrderAssignment(acceptedAssignment.viewAsOrderAssignmentDTO());

        // push to web socket queue
        simpMessagingTemplate.convertAndSend("/topic/hello", List.of(
                orderAssignmentMapper.toOrderAssignmentDTO(acceptedAssignment)));
    }

    private void cancelOrder(OrderOutputDTO orderOutputDTO) {
        log.info("Cancelling preferred order by user:", orderOutputDTO.getOrderRef());

        OrderAssignment openOrderAssignment = orderAssignmentService
                .findByOrderIdAndAssignedStatuses(
                        orderOutputDTO.getId(), List.of(Status.PENDING, Status.ACCEPTED)).get(0);
        openOrderAssignment.setAssignedStatus(Status.CANCELLED);
        orderAssignmentService.saveOrderAssignment(openOrderAssignment);

        // push to web socket queue
        simpMessagingTemplate.convertAndSend("/topic/hello", List.of(openOrderAssignment.viewAsOrderAssignmentDTO()));
    }

    @Override
    public OrderAssignmentDTO match(OrderAssignmentDTO orderAssignmentDTO) {
        OrderAssignment orderAssignment = orderAssignmentMapper.toOrderAssignment(orderAssignmentDTO);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserAdditionalInfo userAdditionalInfo = (UserAdditionalInfo) authentication.getDetails();
        orderAssignment.setOrganizationId(Long.valueOf(userAdditionalInfo.getInternalId()));

        orderAssignmentService.saveOrderAssignment(orderAssignment);
        orderProducer.send(orderAssignment.viewAsOrderAssignmentDTO());

        return orderAssignmentDTO;
    }

    @Override
    public void match() {
        //TODO: do we really need to close this order

    }
}
