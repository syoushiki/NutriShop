package com.healthshop.controller;

import com.healthshop.entity.Product;
import com.healthshop.repo.ProductRepository;
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
  public ResponseEntity<Product> get(@PathVariable Long id) {
    return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }
}
