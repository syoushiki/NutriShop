package com.healthshop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ProductRequest {
  @NotBlank(message = "商品名称不能为空")
  private String name;

  private String description;

  private String imageUrl;
  private String imageGallery;

  @NotNull(message = "价格不能为空")
  @DecimalMin(value = "0.0", inclusive = true, message = "价格不能小于0")
  private BigDecimal price;

  private String tags;

  private String targetAudience;

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getImageUrl() { return imageUrl; }
  public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
  public String getImageGallery() { return imageGallery; }
  public void setImageGallery(String imageGallery) { this.imageGallery = imageGallery; }
  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal price) { this.price = price; }
  public String getTags() { return tags; }
  public void setTags(String tags) { this.tags = tags; }
  public String getTargetAudience() { return targetAudience; }
  public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
}
