package com.healthshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class UserProfileRequest {
  @NotNull(message = "userId不能为空")
  private Long userId;

  @NotBlank(message = "年龄段不能为空")
  private String ageGroup;

  @NotBlank(message = "性别不能为空")
  private String gender;

  @NotEmpty(message = "至少选择一个健康诉求")
  private List<String> healthGoals;

  @NotBlank(message = "预算不能为空")
  private String budget;

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getAgeGroup() { return ageGroup; }
  public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
  public String getGender() { return gender; }
  public void setGender(String gender) { this.gender = gender; }
  public List<String> getHealthGoals() { return healthGoals; }
  public void setHealthGoals(List<String> healthGoals) { this.healthGoals = healthGoals; }
  public String getBudget() { return budget; }
  public void setBudget(String budget) { this.budget = budget; }
}
