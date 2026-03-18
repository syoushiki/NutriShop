package com.healthshop.repo;

import com.healthshop.entity.CartItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<CartItem, Long> {
  List<CartItem> findByUsername(String username);
  void deleteByUsername(String username);
}
