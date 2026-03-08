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

        // 클라이언트 IP 추출
        String clientIp = getClientIp(request);
        request.setAttribute("clientIp", clientIp);

        // 요청 로그 출력
        log.info("[REQUEST] [{}] {} {} | IP: {}", requestId, request.getMethod(), request.getRequestURI(), clientIp);

        return true;
    }

    private String getClientIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 다중 프록시를 거칠 경우 콤마(,)로 구분되어 첫 번째 IP가 실제 클라이언트 IP임
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
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
