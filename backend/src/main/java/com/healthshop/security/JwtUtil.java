package com.healthshop.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  @Value("${security.jwt.secret}")
  private String secret;
  @Value("${security.jwt.expiration}")
  private long expiration;

  private Key getKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(String subject) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + expiration);
    return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(now)
        .setExpiration(exp)
        .signWith(getKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String getSubject(String token) {
    return Jwts.parserBuilder().setSigningKey(getKey()).build()
        .parseClaimsJws(token).getBody().getSubject();
  }
}
