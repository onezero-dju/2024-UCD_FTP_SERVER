package com.ucd.exampleftp.util.config.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 커스텀 UserDetails 구현체로, userId를 포함합니다.
 */


public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String email;
    private final Long userId;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String username, String email, Long userId, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.email = email;
        this.userId = userId;
        this.authorities = authorities;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // 비밀번호는 JWT 기반 인증이므로 null 반환
    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // 계정 상태 관련 메서드들은 모두 true로 설정
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
