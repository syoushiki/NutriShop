package com.healthshop.controller;

import com.healthshop.dto.ProductRequest;
import com.healthshop.entity.Product;
import com.healthshop.repo.ProductRepository;
import com.healthshop.service.ProductRecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  private final ProductRepository repo;
  private final ProductRecommendationService recommendationService;

  public ProductController(ProductRepository repo, ProductRecommendationService recommendationService) {
    this.repo = repo;
    this.recommendationService = recommendationService;
  }

  @GetMapping
  public List<Product> list() {
    return repo.findAll();
  }

  @GetMapping("/recommend")
  public List<Product> recommend(@RequestParam("userId") Long userId) {
    return recommendationService.recommend(userId);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Product> get(@PathVariable("id") Long id) {
    return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
    Product product = new Product();
    product.setName(request.getName().trim());
    product.setDescription(normalizeNullableText(request.getDescription()));
    product.setImageUrl(normalizeNullableText(request.getImageUrl()));
    product.setPrice(request.getPrice());
    product.setTags(normalizeNullableText(request.getTags()));
    product.setTargetAudience(normalizeNullableText(request.getTargetAudience()));
    Product saved = repo.save(product);
    return ResponseEntity.ok(saved);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Product> update(@PathVariable("id") Long id, @Valid @RequestBody ProductRequest request) {
    return repo.findById(id)
        .map(existing -> {
          existing.setName(request.getName().trim());
          existing.setDescription(normalizeNullableText(request.getDescription()));
          existing.setImageUrl(normalizeNullableText(request.getImageUrl()));
          existing.setPrice(request.getPrice());
          existing.setTags(normalizeNullableText(request.getTags()));
          existing.setTargetAudience(normalizeNullableText(request.getTargetAudience()));
          return ResponseEntity.ok(repo.save(existing));
        })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
    if (repo.existsById(id)) {
      repo.deleteById(id);
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }

  private String normalizeNullableText(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    if (trimmed.isEmpty()) return null;
    return trimmed;
  }
}