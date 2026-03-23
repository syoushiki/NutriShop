package com.healthshop.controller;

import com.healthshop.dto.OrderStatusUpdateRequest;
import com.healthshop.entity.CartItem;
import com.healthshop.entity.OrderItem;
import com.healthshop.entity.ShopOrder;
import com.healthshop.entity.User;
import com.healthshop.repo.CartRepository;
import com.healthshop.repo.OrderItemRepository;
import com.healthshop.repo.ShopOrderRepository;
import com.healthshop.repo.UserRepository;
import com.healthshop.security.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final CartRepository cartRepository;
  private final ShopOrderRepository shopOrderRepository;
  private final OrderItemRepository orderItemRepository;
  private final UserRepository userRepository;

  public OrderController(CartRepository cartRepository,
                         ShopOrderRepository shopOrderRepository,
                         OrderItemRepository orderItemRepository,
                         UserRepository userRepository) {
    this.cartRepository = cartRepository;
    this.shopOrderRepository = shopOrderRepository;
    this.orderItemRepository = orderItemRepository;
    this.userRepository = userRepository;
  }

  @PostMapping("/submit-from-cart")
  public ResponseEntity<?> submitFromCart() {
    String username = currentUser();
    if (isAnonymous(username)) return ResponseEntity.status(401).build();

    List<CartItem> cartItems = cartRepository.findByUsername(username);
    if (cartItems.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("message", "购物车为空，无法提交订单"));
    }

    User user = userRepository.findByUsername(username).orElse(null);

    BigDecimal total = cartItems.stream()
        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    ShopOrder order = new ShopOrder();
    order.setUsername(username);
    order.setUserAddress(user == null ? null : user.getAddress());
    order.setTotalAmount(total);
    order.setConfirmed(false);
    order.setShipped(false);
    ShopOrder savedOrder = shopOrderRepository.save(order);

    List<OrderItem> orderItems = new ArrayList<>();
    for (CartItem cartItem : cartItems) {
      OrderItem orderItem = new OrderItem();
      orderItem.setOrderId(savedOrder.getId());
      orderItem.setProductId(cartItem.getProductId());
      orderItem.setProductName(cartItem.getName());
      orderItem.setUnitPrice(cartItem.getPrice());
      orderItem.setQuantity(cartItem.getQuantity());
      orderItem.setSubtotal(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
      orderItems.add(orderItem);
    }
    orderItemRepository.saveAll(orderItems);

    cartRepository.deleteByUsername(username);

    return ResponseEntity.ok(buildOrderResponse(savedOrder, orderItems));
  }

  @GetMapping("/my")
  public ResponseEntity<?> myOrders() {
    String username = currentUser();
    if (isAnonymous(username)) return ResponseEntity.status(401).build();
    return ResponseEntity.ok(buildOrdersResponse(shopOrderRepository.findByUsernameOrderByCreatedAtDesc(username)));
  }

  @GetMapping("/admin")
  public ResponseEntity<?> adminOrders() {
    User current = currentUserEntity();
    if (current == null) return ResponseEntity.status(401).build();
    if (current.getRole() != Role.ADMIN) return ResponseEntity.status(403).build();
    return ResponseEntity.ok(buildOrdersResponse(shopOrderRepository.findAllByOrderByCreatedAtDesc()));
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<?> updateStatus(@PathVariable("id") Long id, @RequestBody OrderStatusUpdateRequest request) {
    User current = currentUserEntity();
    if (current == null) return ResponseEntity.status(401).build();
    if (current.getRole() != Role.ADMIN) return ResponseEntity.status(403).build();

    return shopOrderRepository.findById(id)
        .map(order -> {
          if (request.getConfirmed() != null) {
            order.setConfirmed(request.getConfirmed());
          }
          if (request.getShipped() != null) {
            order.setShipped(request.getShipped());
          }
          ShopOrder saved = shopOrderRepository.save(order);
          List<OrderItem> items = orderItemRepository.findByOrderId(saved.getId());
          return ResponseEntity.ok(buildOrderResponse(saved, items));
        })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}/cancel")
  public ResponseEntity<?> cancelOwnOrder(@PathVariable("id") Long id) {
    String username = currentUser();
    if (isAnonymous(username)) return ResponseEntity.status(401).build();

    return shopOrderRepository.findById(id)
        .map(order -> {
          if (!username.equals(order.getUsername())) {
            return ResponseEntity.status(403).body(Map.of("message", "只能取消自己的订单"));
          }
          if (Boolean.TRUE.equals(order.getConfirmed())) {
            return ResponseEntity.badRequest().body(Map.of("message", "订单已确认，无法取消"));
          }
          orderItemRepository.deleteByOrderId(order.getId());
          shopOrderRepository.delete(order);
          return ResponseEntity.ok(Map.of("message", "订单已取消"));
        })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  private List<Map<String, Object>> buildOrdersResponse(List<ShopOrder> orders) {
    if (orders.isEmpty()) return List.of();
    List<Long> ids = orders.stream().map(ShopOrder::getId).toList();
    List<OrderItem> items = orderItemRepository.findByOrderIdIn(ids);
    Map<Long, List<OrderItem>> grouped = items.stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
    return orders.stream().map(order -> buildOrderResponse(order, grouped.getOrDefault(order.getId(), List.of()))).toList();
  }

  private Map<String, Object> buildOrderResponse(ShopOrder order, List<OrderItem> items) {
    Map<String, Object> m = new HashMap<>();
    m.put("id", order.getId());
    m.put("username", order.getUsername());
    m.put("userAddress", order.getUserAddress() == null ? "" : order.getUserAddress());
    m.put("totalAmount", order.getTotalAmount());
    m.put("confirmed", order.getConfirmed());
    m.put("shipped", order.getShipped());
    m.put("createdAt", order.getCreatedAt());
    m.put("items", items);
    return m;
  }

  private String currentUser() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    return a != null ? String.valueOf(a.getPrincipal()) : "guest";
  }

  private User currentUserEntity() {
    String username = currentUser();
    if (isAnonymous(username)) return null;
    return userRepository.findByUsername(username).orElse(null);
  }

  private boolean isAnonymous(String username) {
    return username == null || username.isBlank() || "guest".equals(username) || "anonymousUser".equals(username);
  }
}
