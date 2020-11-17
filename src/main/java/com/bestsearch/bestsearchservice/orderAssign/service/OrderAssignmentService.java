package com.bestsearch.bestsearchservice.orderAssign.service;


import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.orderAssign.mapper.OrderAssignmentMapper;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignStatus;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;
import com.bestsearch.bestsearchservice.orderAssign.repository.OrderAssignmentRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderAssignmentService {

  @Autowired
  OrderAssignmentRepository orderAssignmentRepository;

  @Autowired
  OrderAssignmentMapper orderAssignmentMapper;

  public void saveOrderAssignment(List<OrderAssignment> assignments){
    orderAssignmentRepository.saveAll(assignments);
  }

  // TODO: is this the best way of handling?
  public void updateOrderAssignmentStatus(OrderAssignmentDTO orderAssignmentDTO){
    if(orderAssignmentDTO.getOrderType() == OrderType.IMMEDIATE){

      if(orderAssignmentDTO.getAssignedStatus() == OrderAssignStatus.REJECTED){
        // update order assignment status
        orderAssignmentRepository.save(orderAssignmentMapper.toOrderAssignment(orderAssignmentDTO));
      } else if (orderAssignmentDTO.getAssignedStatus() == OrderAssignStatus.ACCEPTED){
        // update order assignment status
        orderAssignmentRepository.save(orderAssignmentMapper.toOrderAssignment(orderAssignmentDTO));

        // TODO: update order status and assign organization
      }

    } else if(orderAssignmentDTO.getOrderType() == OrderType.CLOSEST) {

      if(orderAssignmentDTO.getAssignedStatus() == OrderAssignStatus.REJECTED){
        // update order assignment status
        orderAssignmentRepository.save(orderAssignmentMapper.toOrderAssignment(orderAssignmentDTO));

        // Get next order assignment and send
        List<OrderAssignment> orderAssignments = orderAssignmentRepository.findByOrderId();


      } else if (orderAssignmentDTO.getAssignedStatus() == OrderAssignStatus.ACCEPTED){
        // update order assignment status
        orderAssignmentRepository.save(orderAssignmentMapper.toOrderAssignment(orderAssignmentDTO));

        // update order status and assign organization
      }
    }
  }

}
