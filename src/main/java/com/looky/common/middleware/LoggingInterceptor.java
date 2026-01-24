package com.looky.common.middleware;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 요청 식별 고유 ID 생성
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        request.setAttribute("requestId", requestId);

        // 시작 시간 기록
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        // 요청 로그 출력
        log.info("[REQUEST] [{}] {} {}", requestId, request.getMethod(), request.getRequestURI());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        String requestId = (String) request.getAttribute("requestId");
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        String errorInfo = "";
        if (ex != null) {
            errorInfo = " | Exception: " + ex.getMessage();
        }

        // 응답 로그 출력
        log.info("[RESPONSE] [{}] {} {} | Status: {} | Time: {}ms{}",
                requestId, request.getMethod(), request.getRequestURI(), response.getStatus(), duration, errorInfo);
    }
}
