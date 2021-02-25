package com.bestsearch.bestsearchservice.orderAssign.dto;

import com.bestsearch.bestsearchservice.order.model.enums.OrderType;

import com.bestsearch.bestsearchservice.order.model.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OrderAssignmentDTO {
  private Long id;
  private Long orderId;
  private Long organizationId;
  private LocalDateTime assignedAt;
  private Status assignedStatus;
  private OrderType orderType;
  private int priority;
  private String userComment;

  @JsonIgnore
  public LocalDate getAssignedDate() {
    return assignedAt.toLocalDate();
  }

}
