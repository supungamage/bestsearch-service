package com.bestsearch.bestsearchservice.orderAssign.repository;


import com.bestsearch.bestsearchservice.orderAssign.model.OrderAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderAssignmentRepository extends JpaRepository<OrderAssignment,Long> {

  public List<OrderAssignment> findByOrderId(Long id);

}
