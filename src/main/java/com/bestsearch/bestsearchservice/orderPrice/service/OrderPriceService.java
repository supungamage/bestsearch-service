package com.bestsearch.bestsearchservice.orderPrice.service;

import com.bestsearch.bestsearchservice.orderPrice.model.OrderPrice;
import com.bestsearch.bestsearchservice.orderPrice.repository.OrderPriceRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderPriceService {

  private final OrderPriceRepository orderPriceRepository;

  public OrderPriceService(final OrderPriceRepository orderPriceRepository) {
    this.orderPriceRepository = orderPriceRepository;
  }

  public OrderPrice getOrderPriceByOrderID(Long orderId) {
    return this.orderPriceRepository.getOrderPriceByOrderId(orderId).orElseThrow();
  }

  public Map<Long, OrderPrice> getOrderPricesByOrderIDs(List<Long> orderIds) {
    return this.orderPriceRepository.findByOrderIdIn(orderIds).stream()
        .collect(Collectors.toMap(OrderPrice::getOrderId, v -> v));


  }

  @Transactional
  public OrderPrice saveOrderPrice(OrderPrice orderPrice) {
    return orderPriceRepository.save(orderPrice);
  }

}
