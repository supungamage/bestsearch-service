package com.bestsearch.bestsearchservice.order.model.custom;

import com.bestsearch.bestsearchservice.order.model.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
public class OrdersByDate {
    LocalDate orderedAt;
    List<Order> orders;
}
