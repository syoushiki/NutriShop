package com.healthshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Column(name = "age_group", nullable = false, length = 50)
  private String ageGroup;

  @Column(nullable = false, length = 50)
  private String gender;

  @Column(name = "health_goals", length = 500)
  private String healthGoals;

  @Column(nullable = false, length = 50)
  private String budget;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getAgeGroup() { return ageGroup; }
  public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
  public String getGender() { return gender; }
  public void setGender(String gender) { this.gender = gender; }
  public String getHealthGoals() { return healthGoals; }
  public void setHealthGoals(String healthGoals) { this.healthGoals = healthGoals; }
  public String getBudget() { return budget; }
  public void setBudget(String budget) { this.budget = budget; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
