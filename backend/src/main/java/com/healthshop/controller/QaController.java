package com.healthshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/qa")
public class QaController {
  @PostMapping("/ask")
  public Map<String, Object> ask(@RequestBody Map<String, Object> body) {
    String question = String.valueOf(body.getOrDefault("question", ""));
    Map<String, Object> m = new HashMap<>();
    m.put("answer", "这是占位回答，建议根据国家标准评估个人剂量与搭配。");
    m.put("reason", "基于营养素参考摄入量与常见成分相互作用的通用规则");
    m.put("question", question);
    return m;
  }
  @GetMapping("/history")
  public List<Map<String, Object>> history() {
    return List.of();
  }
}
