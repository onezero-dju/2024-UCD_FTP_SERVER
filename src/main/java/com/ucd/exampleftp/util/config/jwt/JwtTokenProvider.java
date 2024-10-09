package com.ucd.exampleftp.util.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {
    private SecretKey secretKey;

    @Value("${spring.jwt.secret}")
    private String secret;
    @Value("${spring.jwt.expiration}")
    private long validityInMilliseconds;

    @PostConstruct
    protected void init() {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * JWT 토큰 생성 메서드
     *
     * @param email    사용자 이메일
     * @param username 사용자명
     * @param role     사용자 역할
     * @param userId   사용자 ID
     * @param expiredMs 토큰 만료 시간 (밀리초 단위)
     * @return 생성된 JWT 토큰
     */
    public String createToken(String email, String username, String role, Long userId, Long expiredMs) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("user_id", userId);

        Date now = new Date();
        Date validity = new Date(now.getTime() + expiredMs);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey) // SecretKey 객체로 서명
                .compact();
    }

    /**
     * JWT 토큰 검증 메서드
     *
     * @param token 검증할 JWT 토큰
     * @return 유효한 토큰인지 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 로그 추가 가능
            return false;
        }
    }

    /**
     * JWT 토큰에서 사용자명 추출 메서드
     *
     * @param token JWT 토큰
     * @return 사용자명
     */
    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * JWT 토큰에서 이메일 추출 메서드
     *
     * @param token JWT 토큰
     * @return 사용자 이메일
     */
    public String getEmail(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("email", String.class);
    }

    /**
     * JWT 토큰에서 역할 추출 메서드
     *
     * @param token JWT 토큰
     * @return 사용자 역할
     */
    public String getRole(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

    /**
     * JWT 토큰에서 사용자 ID 추출 메서드
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("user_id", Long.class);
    }

    /**
     * JWT 토큰 만료 여부 확인 메서드
     *
     * @param token JWT 토큰
     * @return 토큰이 만료되었는지 여부
     */
    public Boolean isExpired(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().before(new Date());
    }
}
