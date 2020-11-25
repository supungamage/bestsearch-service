package com.bestsearch.bestsearchservice.orderAssign.model;

import com.bestsearch.bestsearchservice.order.dto.OrderOutputDTO;
import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.orderAssign.dto.OrderAssignmentDTO;
import com.bestsearch.bestsearchservice.share.audit.Auditable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "order_assignment")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAssignment extends Auditable<String> {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="order_assign_seq")
  @SequenceGenerator(name = "order_assign_seq", sequenceName = "order_assign_seq" ,allocationSize = 1)
  private Long id;
  private Long orderId;
  private Long organizationId;
  private LocalDateTime assignedDate;
  @Enumerated(EnumType.ORDINAL)
  private OrderAssignStatus assignedStatus;
  @Enumerated(EnumType.ORDINAL)
  private OrderType orderType;
  private int priority;

  @JsonIgnore
  public OrderAssignmentDTO viewAsOrderAssignmentDTO() {
    return OrderAssignmentDTO.builder().id(id).orderId(orderId).organizationId(organizationId)
            .assignedDate(assignedDate).assignedStatus(assignedStatus).orderType(orderType)
            .priority(priority)
            .build();
  }
}
