package com.bestsearch.bestsearchservice.orderPrice.model;

import com.bestsearch.bestsearchservice.share.audit.Auditable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "order_price")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPrice extends Auditable<String> {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_price_seq")
  @SequenceGenerator(name = "order_price_seq", sequenceName = "order_price_seq", allocationSize = 1)
  private Long id;

  private Long orderId;
  private Long orderAssignmentId;
  private Double originalPrice;
  private Double alternatePrice;
  private String additionalDetails;


}
