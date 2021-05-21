package com.bestsearch.bestsearchservice.orderPrice.repository;

import com.bestsearch.bestsearchservice.orderPrice.model.OrderPrice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderPriceRepository extends JpaRepository<OrderPrice, Long> {

  Optional<OrderPrice> getOrderPriceByOrderId(Long orderId);

  List<OrderPrice> findByOrderIdIn(List<Long> orderIds);

}
