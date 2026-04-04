package com.project.base_v1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class JwtTokenProvider {

    Key key;
    long accessTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long expiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = expiration * 1000;
    }

    public String generateAccessToken(UUID userId, String username, String role, UUID patientId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        JwtBuilder builder = Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("role", role);

        if (patientId != null) {
            builder.claim("patientId", patientId.toString());
        }

        return builder
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public UUID getUserId(String token) {
        return UUID.fromString(validateToken(token).getBody().getSubject());
    }

    public String generateRefreshToken(UUID sessionId) {
        return Jwts.builder()
                .setSubject(sessionId.toString())
                .setIssuedAt(new Date())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
