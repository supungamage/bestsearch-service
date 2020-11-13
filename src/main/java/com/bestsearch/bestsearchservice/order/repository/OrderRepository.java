package com.bestsearch.bestsearchservice.order.repository;

import com.bestsearch.bestsearchservice.order.model.Order;
import com.bestsearch.bestsearchservice.order.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderRefAndOrganizationTypeId(String orderRef, long organizationTypeId);

    @Modifying
    @Query(value = "UPDATE Order o SET o.status = ?2 WHERE o.id = ?1")
    void updateOrderStatus(long id, Status toStatus);

    @Query(value = "SELECT nextval('order_seq')", nativeQuery = true)
    long getNextId();

    @Query(value = "SELECT o FROM Order o " +
            "WHERE o.organizationTypeId = :organizationTypeId " +
            "and o.userId = :userId " +
            "and o.status <> :status " +
            "ORDER BY o.orderedAt")
    Optional<List<Order>> getCurrentOrders(long organizationTypeId, long userId, Status status);

    @Query(value = "SELECT o FROM Order o " +
            "WHERE o.organizationTypeId = :organizationTypeId " +
            "and o.userId = :userId " +
            "and o.status = :status " +
            "ORDER BY o.orderedAt")
    Optional<List<Order>> getPastOrders(long organizationTypeId, long userId, Status status);


}
