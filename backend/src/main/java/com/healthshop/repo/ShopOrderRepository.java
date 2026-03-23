package com.healthshop.repo;

import com.healthshop.entity.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {
  List<ShopOrder> findByUsernameOrderByCreatedAtDesc(String username);
  List<ShopOrder> findAllByOrderByCreatedAtDesc();
}