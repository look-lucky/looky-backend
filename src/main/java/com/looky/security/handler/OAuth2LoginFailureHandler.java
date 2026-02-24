package com.looky.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.looky.common.exception.ErrorCode;
import com.looky.common.response.CommonResponse;
import com.looky.common.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        
        log.error("OAuth2 Login 실패: {}", exception.getMessage());
        
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        String message = exception.getMessage();
        if (message == null || message.isEmpty()) {
            message = "소셜 로그인 인증에 실패했습니다.";
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                message,
                request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(CommonResponse.fail(errorResponse)));
    }
}
