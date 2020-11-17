package com.bestsearch.bestsearchservice.orderAssign.dto;

import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderAssignmentDTO {
  private Long id;
  private Long orderId;
  private Long organizationId;
  private Date assignedDate;
  private OrderAssignStatus assignedStatus;
  private OrderType orderType;

}
