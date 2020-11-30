package com.bestsearch.bestsearchservice.orderAssign.matchingEngine;

import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import org.springframework.stereotype.Component;

@Component
public class MatchPreferred implements IMatchBehaviour {

    @Override
    public void match(OrderOutputDTO orderOutputDTO) {

    }

    @Override
    public OrderAssignmentDTO match(OrderAssignmentDTO orderAssignmentDTO) {
        return orderAssignmentDTO;
    }

    @Override
    public void match() {

    }
}
