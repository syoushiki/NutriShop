package com.healthshop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/pay")
public class PaymentController {
  @Value("${alipay.server-url}")
  private String serverUrl;

  @PostMapping("/create")
  public Map<String, Object> create(@RequestBody Map<String, Object> body) {
    String outTradeNo = UUID.randomUUID().toString().replace("-", "");
    String amount = String.valueOf(body.getOrDefault("amount", "0.01"));
    String subject = "健康商城订单";
    String url = serverUrl + "?biz_content=" + URLEncoder.encode("{\"out_trade_no\":\"" + outTradeNo + "\",\"total_amount\":\"" + amount + "\",\"subject\":\"" + subject + "\"}", StandardCharsets.UTF_8) + "&method=alipay.trade.page.pay";
    Map<String, Object> m = new HashMap<>();
    m.put("payUrl", url);
    m.put("outTradeNo", outTradeNo);
    return m;
  }

  @PostMapping("/notify")
  public ResponseEntity<String> notifyCallback() {
    return ResponseEntity.ok("success");
  }
}
