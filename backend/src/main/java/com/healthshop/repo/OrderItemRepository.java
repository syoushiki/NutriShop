package com.healthshop.repo;

import com.healthshop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
  List<OrderItem> findByOrderIdIn(List<Long> orderIds);
  List<OrderItem> findByOrderId(Long orderId);
  void deleteByOrderId(Long orderId);
}
