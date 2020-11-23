package com.bestsearch.bestsearchservice.orderAssign.mapper;

import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderAssignmentMapper {

  OrderAssignmentDTO toOrderAssignmentDTO(OrderAssignment orderAssignment);

  OrderAssignment toOrderAssignment(OrderAssignmentDTO orderAssignmentDTO);

}

