package com.healthshop.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(fieldError -> fieldError.getDefaultMessage())
        .orElse("请求参数不合法");
    return build(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "请求体格式错误", request.getRequestURI());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "商品字段不符合数据库约束", request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleAny(Exception ex, HttpServletRequest request) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage() == null ? "服务器内部错误" : ex.getMessage(), request.getRequestURI());
  }

  private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, String path) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);
    body.put("path", path);
    return ResponseEntity.status(status).body(body);
  }
}
