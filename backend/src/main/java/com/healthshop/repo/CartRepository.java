package com.healthshop.repo;

import com.healthshop.entity.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<CartItem, Long> {
  List<CartItem> findByUsername(String username);
  Optional<CartItem> findByIdAndUsername(Long id, String username);
  Optional<CartItem> findByUsernameAndProductId(String username, Long productId);
  void deleteByUsername(String username);
}
