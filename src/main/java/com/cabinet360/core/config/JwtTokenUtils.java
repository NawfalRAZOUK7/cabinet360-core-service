package com.cabinet360.core.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtTokenUtils {

    private final Key jwtSecretKey;

    public JwtTokenUtils(@Value("${jwt.secret}") String secret) {
        this.jwtSecretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Valide le token (signature, expiration)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Récupère le "username" (souvent email) du token
    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    // Récupère les rôles (doit correspondre à la clé utilisée dans auth-service)
    public List<String> getRolesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object rolesObj = claims.get("role");
        if (rolesObj instanceof List<?> rolesList) {
            List<String> result = new ArrayList<>();
            for (Object role : rolesList) {
                result.add(String.valueOf(role));
            }
            return result;
        } else if (rolesObj instanceof String rolesString) {
            // Parfois c'est une string séparée par des virgules
            return Arrays.asList(rolesString.split(","));
        }
        return Collections.emptyList();
    }

    // Accès à toutes les claims
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
