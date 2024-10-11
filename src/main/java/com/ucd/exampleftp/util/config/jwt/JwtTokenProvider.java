package com.ucd.exampleftp.util.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * JWT 토큰의 생성 및 검증을 담당하는 클래스
 */
@Component
public class JwtTokenProvider {
    private SecretKey secretKey;

    @Value("${spring.jwt.secret}")
    private String secret;

    @Value("${spring.jwt.expiration}")
    private long validityInMilliseconds;

    @PostConstruct
    protected void init() {
        // HS256 알고리즘을 사용하여 비밀 키를 초기화
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * JWT 토큰 생성 메서드
     *
     * @param email     사용자 이메일
     * @param username  사용자명
     * @param role      사용자 역할
     * @param userId    사용자 ID
     * @param expiredMs 토큰 만료 시간 (밀리초 단위)
     * @return 생성된 JWT 토큰
     */
    public String createJwt(String email, String username, String role, Long userId, Long expiredMs) {
        return Jwts.builder()
                .claim("email", email)
                .claim("username", username)
                .claim("role", role)
                .claim("user_id", userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
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
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 로그 추가 가능
            return false;
        }
    }

    /**
     * JWT 토큰에서 모든 클레임을 추출하여 CustomUserDetails 객체를 반환하는 메서드
     *
     * @param token JWT 토큰
     * @return CustomUserDetails 객체
     */
    public CustomUserDetails getUserDetails(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.get("username", String.class);
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);
        Long userId = claims.get("user_id", Long.class);

        Collection<? extends GrantedAuthority> authorities = getAuthorities(role);

        return new CustomUserDetails(username, email, userId, authorities);
    }

    /**
     * JWT 토큰에서 역할 기반 GrantedAuthority 생성 메서드
     *
     * @param role 사용자 역할
     * @return GrantedAuthority 컬렉션
     */
    public Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return List.of(new SimpleGrantedAuthority(role));
    }

    // 기타 클레임 추출 메서드들이 필요 없다면 제거할 수 있습니다.
}
