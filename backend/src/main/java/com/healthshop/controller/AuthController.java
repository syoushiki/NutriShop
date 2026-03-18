package com.healthshop.controller;

import com.healthshop.dto.AuthDtos.LoginRequest;
import com.healthshop.dto.AuthDtos.RegisterRequest;
import com.healthshop.dto.AuthDtos.TokenResponse;
import com.healthshop.entity.User;
import com.healthshop.repo.UserRepository;
import com.healthshop.security.JwtUtil;
import com.healthshop.security.Role;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository userRepository;
  private final PasswordEncoder encoder;
  private final JwtUtil jwtUtil;
  public AuthController(UserRepository userRepository, PasswordEncoder encoder, JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.encoder = encoder;
    this.jwtUtil = jwtUtil;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
    if (userRepository.findByUsername(req.getUsername()).isPresent()) {
      return ResponseEntity.badRequest().body("Username is already taken");
    }
    if (userRepository.findByEmail(req.getEmail()).isPresent()) {
      return ResponseEntity.badRequest().body("Email is already registered");
    }
    if (userRepository.findByPhone(req.getPhone()).isPresent()) {
      return ResponseEntity.badRequest().body("Phone number is already registered");
    }
    User u = new User();
    u.setUsername(req.getUsername());
    u.setPassword(encoder.encode(req.getPassword()));
    u.setEmail(req.getEmail());
    u.setPhone(req.getPhone());
    u.setRole(Role.USER);
    userRepository.save(u);
    String token = jwtUtil.generateToken(u.getUsername());
    return ResponseEntity.ok(new TokenResponse(token));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
    return userRepository.findByUsername(req.getUsername())
        .filter(u -> encoder.matches(req.getPassword(), u.getPassword()))
        .map(u -> ResponseEntity.ok(new TokenResponse(jwtUtil.generateToken(u.getUsername()))))
        .orElseGet(() -> ResponseEntity.status(401).build());
  }

  @GetMapping("/me")
  public ResponseEntity<?> me(@RequestHeader("Authorization") String authorization) {
    String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
    if (token == null) return ResponseEntity.status(401).build();
    String subject = jwtUtil.getSubject(token);
    return userRepository.findByUsername(subject)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(401).build());
  }
}
