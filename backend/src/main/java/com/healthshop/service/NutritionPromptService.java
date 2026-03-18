package com.healthshop.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class NutritionPromptService {
  public String buildPrompt(Map<String, Object> context) {
    return "依据《居民膳食营养素参考摄入量》生成的评估提示，结合用户健康档案与购物车成分，输出风险与建议。";
  }
}
