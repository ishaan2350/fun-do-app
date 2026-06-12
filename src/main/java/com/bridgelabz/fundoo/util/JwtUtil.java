package com.bridgelabz.fundoo.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${fundoo.jwt.secret}")
    private String secret;

    @Value("${fundoo.jwt.expiration}")
    private long jwtExpiration;

    @Value("${fundoo.jwt.verification.expiration}")
    private long verificationExpiration;

    @Value("${fundoo.jwt.reset.expiration}")
    private long resetExpiration;

    private Key getSigningKey() {
        byte[] keyBytes = this.secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate token for login
    public String generateToken(String email) {
        return createToken(new HashMap<>(), email, jwtExpiration);
    }

    // Generate token for email verification
    public String generateVerificationToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "verify-email");
        return createToken(claims, email, verificationExpiration);
    }

    // Generate token for password reset
    public String generateResetToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "reset-password");
        return createToken(claims, email, resetExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Validate token
    public boolean validateToken(String token, String email) {
        try {
            final String username = extractEmail(token);
            return (username.equals(email) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateVerificationToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String purpose = claims.get("purpose", String.class);
            return "verify-email".equals(purpose) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Verification JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateResetToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String purpose = claims.get("purpose", String.class);
            return "reset-password".equals(purpose) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Reset JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
