package com.healthshop.controller;

import com.healthshop.entity.CartItem;
import com.healthshop.entity.Product;
import com.healthshop.repo.CartRepository;
import com.healthshop.repo.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
  private final CartRepository cartRepo;
  private final ProductRepository productRepo;
  public CartController(CartRepository cartRepo, ProductRepository productRepo) {
    this.cartRepo = cartRepo;
    this.productRepo = productRepo;
  }
  private String currentUser() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    return a != null ? String.valueOf(a.getPrincipal()) : "guest";
  }
  @GetMapping
  public Map<String, Object> get() {
    String user = currentUser();
    if ("anonymousUser".equals(user) || "guest".equals(user)) {
      Map<String, Object> m = new HashMap<>();
      m.put("items", List.of());
      return m;
    }
    List<CartItem> items = cartRepo.findByUsername(user);
    Map<String, Object> m = new HashMap<>();
    m.put("items", items);
    return m;
  }
  @PostMapping("/add")
  public ResponseEntity<?> add(@RequestBody Map<String, Object> body) {
    Long productId = Long.valueOf(String.valueOf(body.get("productId")));
    Integer quantity = Integer.valueOf(String.valueOf(body.getOrDefault("quantity", 1)));
    Product p = productRepo.findById(productId).orElse(null);
    if (p == null) return ResponseEntity.notFound().build();
    CartItem ci = new CartItem();
    ci.setUsername(currentUser());
    ci.setProductId(p.getId());
    ci.setName(p.getName());
    ci.setPrice(p.getPrice());
    ci.setQuantity(quantity);
    cartRepo.save(ci);
    return ResponseEntity.ok().build();
  }
  @PostMapping("/evaluate")
  public Map<String, Object> evaluate(@RequestBody Map<String, Object> body) {
    List<CartItem> items = cartRepo.findByUsername(currentUser());
    BigDecimal total = items.stream().map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
    Map<String, Object> r = new HashMap<>();
    if (total.compareTo(new BigDecimal("500")) > 0) {
      r.put("message", "购物金额较高，请合理搭配，避免剂量超标");
    }
    return r;
  }
}
