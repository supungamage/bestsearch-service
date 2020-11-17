package com.bestsearch.bestsearchservice.orderAssign.model;

import com.bestsearch.bestsearchservice.order.model.enums.OrderType;
import com.bestsearch.bestsearchservice.share.audit.Auditable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "order_assignment")
@Getter
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
  private Date assignedDate;
  @Enumerated(EnumType.ORDINAL)
  private OrderAssignStatus assignedStatus;
  @Enumerated(EnumType.ORDINAL)
  private OrderType orderType;
}
