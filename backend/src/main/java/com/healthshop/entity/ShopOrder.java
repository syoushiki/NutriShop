package com.healthshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_orders")
public class ShopOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String username;

  @Column(name = "user_address", length = 500)
  private String userAddress;

  @Column(name = "total_amount", nullable = false)
  private BigDecimal totalAmount = BigDecimal.ZERO;

  @Column(nullable = false)
  private Boolean confirmed = false;

  @Column(nullable = false)
  private Boolean shipped = false;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    createdAt = LocalDateTime.now();
    if (confirmed == null) confirmed = false;
    if (shipped == null) shipped = false;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getUserAddress() { return userAddress; }
  public void setUserAddress(String userAddress) { this.userAddress = userAddress; }
  public BigDecimal getTotalAmount() { return totalAmount; }
  public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
  public Boolean getConfirmed() { return confirmed; }
  public void setConfirmed(Boolean confirmed) { this.confirmed = confirmed; }
  public Boolean getShipped() { return shipped; }
  public void setShipped(Boolean shipped) { this.shipped = shipped; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
