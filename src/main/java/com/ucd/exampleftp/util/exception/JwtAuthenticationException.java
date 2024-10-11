package com.ucd.exampleftp.util.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * JWT 인증 관련 예외를 처리하는 사용자 정의 예외 클래스
 */
public class JwtAuthenticationException extends AuthenticationException {
    public JwtAuthenticationException(String msg) {
        super(msg);
    }

    public JwtAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
