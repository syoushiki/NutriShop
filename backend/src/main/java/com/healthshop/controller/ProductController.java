package com.healthshop.controller;

import com.healthshop.entity.Product;
import com.healthshop.dto.ProductRequest;
import com.healthshop.repo.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  private final ProductRepository repo;
  public ProductController(ProductRepository repo) { this.repo = repo; }

  @GetMapping
  public List<Product> list() { return repo.findAll(); }

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
