package com.music.infra.security;

import com.music.model.entity.User;
import com.music.model.exceptions.exceptionHandler.AlertException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secretKey;

    @Value("${api.security.token.timeExpiration}")
    private long timeExpirationToken;

    public String generateToken(UserDetails userDetails) {
        Long idUser = null;
        if (userDetails instanceof User) {
            idUser = ((User) userDetails).getIdUser();
        }

        assert idUser != null;
        Map<String, Object> claims = Map.of(
                "id", idUser,
                "roles", userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );
        return buildToken(claims, userDetails);
    }

    public String generateTokenToChangePassword(User user, long timeExpirationToChangePassword) {
        Date expirationTime = Date.from(LocalDateTime.now()
                .plusMinutes(timeExpirationToChangePassword)
                .toInstant(ZoneOffset.of("-03:00")));

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().getRoleName())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationTime)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isSimpleTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    private String buildToken(Map<String, Object> claims, UserDetails userDetails) {
        Date expirationTime = Date.from(LocalDateTime.now()
                .plusHours(timeExpirationToken)
                .toInstant(ZoneOffset.of("-03:00")));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationTime)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new AlertException(
                    "JWT_INVALID",
                    "Token inválido ou expirado",
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
