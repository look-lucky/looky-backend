package com.looky.common.logfilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = UUID.randomUUID().toString().substring(0, 8);   // 요청마다 고유한 ID (Trace ID) 생성

        MDC.put("traceId", traceId);
        MDC.put("clientIp", request.getRemoteAddr());
        MDC.put("requestUri", request.getRequestURI());

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 요청이 끝나면 MDC 상자 비우기
            MDC.clear();
        }
    }
}