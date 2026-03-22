package com.healthshop.controller;

import com.healthshop.dto.UserProfileRequest;
import com.healthshop.entity.UserProfile;
import com.healthshop.repo.UserProfileRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-profiles")
public class UserProfileController {
  private final UserProfileRepository userProfileRepository;

  public UserProfileController(UserProfileRepository userProfileRepository) {
    this.userProfileRepository = userProfileRepository;
  }

  @PostMapping
  public ResponseEntity<UserProfile> upsert(@Valid @RequestBody UserProfileRequest request) {
    UserProfile profile = userProfileRepository.findByUserId(request.getUserId()).orElseGet(UserProfile::new);
    profile.setUserId(request.getUserId());
    profile.setAgeGroup(normalizeRequired(request.getAgeGroup()));
    profile.setGender(normalizeRequired(request.getGender()));
    profile.setBudget(normalizeRequired(request.getBudget()));
    profile.setHealthGoals(request.getHealthGoals().stream()
        .map(this::normalizeRequired)
        .collect(Collectors.joining(",")));
    return ResponseEntity.ok(userProfileRepository.save(profile));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserProfile> getByUserId(@PathVariable Long userId) {
    return userProfileRepository.findByUserId(userId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  private String normalizeRequired(String value) {
    return value == null ? "" : value.trim();
  }
}
