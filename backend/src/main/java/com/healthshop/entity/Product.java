package com.healthshop.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  private String name;
  @Column(length = 2000)
  private String description;
  @Column(name = "image_url")
  private String imageUrl;
  @Lob
  @Column(name = "image_gallery")
  private String imageGallery;
  @Column(nullable = false)
  private BigDecimal price;
  @Column(length = 500)
  private String tags;
  @Column(name = "target_audience", length = 500)
  private String targetAudience;
  @Transient
  private Double relevanceScore;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
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
  public Double getRelevanceScore() { return relevanceScore; }
  public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }
}
