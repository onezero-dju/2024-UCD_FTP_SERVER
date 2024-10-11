package com.ucd.exampleftp.util.config.jwt;

import com.ucd.exampleftp.util.exception.JwtAuthenticationEntryPoint;
import com.ucd.exampleftp.util.exception.JwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 토큰을 검증하고, 인증 정보를 SecurityContext에 설정하는 필터
 */
@Component
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);

            if (token != null) {
                log.info("Received JWT Token: {}", token);

                if (jwtTokenProvider.validateToken(token)) {
                    log.info("JWT Token is valid.");

                    // CustomUserDetails 객체 생성
                    CustomUserDetails userDetails = jwtTokenProvider.getUserDetails(token);

                    // 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // 인증 객체를 SecurityContext에 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.warn("Invalid JWT Token.");
                    throw new JwtAuthenticationException("Invalid JWT Token.");
                }
            } else {
                log.info("No JWT Token found in the request.");
                throw new JwtAuthenticationException("No JWT Token found in the request.");
            }
        } catch (JwtAuthenticationException ex) {
            log.error("JWT Authentication error: {}", ex.getMessage());
            jwtAuthenticationEntryPoint.commence(request, response, ex);
            return;
        } catch (Exception ex) {
            log.error("Exception in JwtTokenFilter: {}", ex.getMessage());
            jwtAuthenticationEntryPoint.commence(request, response, new JwtAuthenticationException("Could not set user authentication in security context", ex));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출 메서드
     *
     * @param request HTTP 요청
     * @return 추출된 JWT 토큰 또는 null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 토큰만 추출
        }
        return null;
    }
}
