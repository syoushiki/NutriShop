package com.healthshop.repo;

import com.healthshop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
  List<OrderItem> findByOrderIdIn(List<Long> orderIds);
  List<OrderItem> findByOrderId(Long orderId);

  @Modifying
  @Transactional
  @Query("delete from OrderItem oi where oi.orderId = :orderId")
  void deleteByOrderId(@Param("orderId") Long orderId);
}
