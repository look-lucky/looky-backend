package com.looky.common.exception;

import static net.logstash.logback.argument.StructuredArguments.kv;
import com.looky.common.response.CommonResponse;
import com.looky.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        // 비즈니스 예외
        @ExceptionHandler(CustomException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleCustomException(CustomException e,
                                                                                   HttpServletRequest request) {
                ErrorCode errorCode = e.getErrorCode();

                log.warn("비즈니스 예외 발생",
                        kv("url", request.getRequestURI()),
                        kv("method", request.getMethod()),
                        kv("errorCode", errorCode.name()),
                        kv("httpStatus", errorCode.getHttpStatus().value()),
                        kv("errorMessage", e.getMessage())
                );

                ErrorResponse errorResponse = ErrorResponse.of(
                        errorCode,
                        e.getMessage(),
                        request.getRequestURI());

                return ResponseEntity
                        .status(errorCode.getHttpStatus())
                        .body(CommonResponse.fail(errorResponse));
        }

        // 입력값 검증 실패
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleValidationException(
                        MethodArgumentNotValidException e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

                log.info("데이터 유효성 검증 실패",
                        kv("url", request.getRequestURI()),
                        kv("method", request.getMethod()),
                        kv("errors", e.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .toList())
                );

                ErrorResponse errorResponse = ErrorResponse.of(
                                errorCode,
                                e.getBindingResult(), // 실패한 필드 정보들
                                request.getRequestURI());

                return ResponseEntity
                                .status(errorCode.getHttpStatus())
                                .body(CommonResponse.fail(errorResponse));
        }

        // 인증 실패 에러
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleAuthenticationException(
                        AuthenticationException e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

                log.warn("인증실패",
                        kv("url", request.getRequestURI()),
                        kv("method", request.getMethod()),
                        kv("errorMessage", e.getMessage())
                );

                ErrorResponse errorResponse = ErrorResponse.of(
                                errorCode,
                                e.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(errorCode.getHttpStatus())
                                .body(CommonResponse.fail(errorResponse));
        }

        // 권한 없음 에러
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleAccessDeniedException(
                        AccessDeniedException e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.FORBIDDEN;

                log.warn("권한 거부",
                        kv("url", request.getRequestURI()),
                        kv("method", request.getMethod()),
                        kv("errorMessage", e.getMessage())
                );

                ErrorResponse errorResponse = ErrorResponse.of(
                                errorCode,
                                e.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(errorCode.getHttpStatus())
                                .body(CommonResponse.fail(errorResponse));
        }

        // 405 에러 - HTTP Method 불일치
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleMethodNotSupportedException(
                        HttpRequestMethodNotSupportedException e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;

                log.warn("지원하지 않는 HTTP Method",
                        kv("url", request.getRequestURI()),
                        kv("method", request.getMethod()),
                        kv("requestedMethod", e.getMethod()),
                        kv("supportedMethods", e.getSupportedMethods())
                );

                ErrorResponse errorResponse = ErrorResponse.of(errorCode, e.getMessage(), request.getRequestURI());

                return ResponseEntity.status(errorCode.getHttpStatus()).body(CommonResponse.fail(errorResponse));
        }

        // 415 에러 - Media Type 불일치
        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleMediaTypeNotSupportedException(
                        HttpMediaTypeNotSupportedException e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.UNSUPPORTED_MEDIA_TYPE;

                log.warn("지원하지 않는 Media Type",
                        kv("url", request.getRequestURI()),
                        kv("method", request.getMethod()),
                        kv("contentType", e.getContentType())
                );

                ErrorResponse errorResponse = ErrorResponse.of(errorCode, e.getMessage(), request.getRequestURI());

                return ResponseEntity.status(errorCode.getHttpStatus()).body(CommonResponse.fail(errorResponse));
        }

        // JSON 파싱 에러 (예: Enum 값 불일치)
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleHttpMessageNotReadableException(
                        HttpMessageNotReadableException e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.BAD_REQUEST;

                log.info("JSON 파싱 오류",
                        kv("url", request.getRequestURI()),
                        kv("method", request.getMethod()),
                        kv("errorMessage", e.getMessage())
                );

                ErrorResponse errorResponse = ErrorResponse.of(errorCode, "잘못된 요청 데이터 형식입니다. (JSON 파싱 오류)", request.getRequestURI());

                return ResponseEntity.status(errorCode.getHttpStatus()).body(CommonResponse.fail(errorResponse));
        }

        // 파라미터 타입 불일치 (예: Long 타입 파라미터에 문자열 입력)
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleMethodArgumentTypeMismatchException(
                        MethodArgumentTypeMismatchException e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.BAD_REQUEST;

                log.info("파라미터 타입 불일치",
                        kv("url", request.getRequestURI()),
                        kv("method", request.getMethod()),
                        kv("paramName", e.getName()),
                        kv("errorMessage", e.getMessage())
                );

                ErrorResponse errorResponse = ErrorResponse.of(errorCode, String.format("잘못된 파라미터 값입니다. (%s)", e.getName()), request.getRequestURI());

                return ResponseEntity.status(errorCode.getHttpStatus()).body(CommonResponse.fail(errorResponse));
        }

        // 정적 리소스 없음 오류 처리
        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleNoResourceFoundException(
                NoResourceFoundException e,
                HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;

                log.info("정적 리소스 없음",
                        kv("url", request.getRequestURI()),
                        kv("method", request.getMethod())
                );

                ErrorResponse errorResponse = ErrorResponse.of(errorCode, e.getMessage(), request.getRequestURI());

                return ResponseEntity.status(errorCode.getHttpStatus()).body(CommonResponse.fail(errorResponse));
        }

        // 나머지 모든 예외 처리
        @ExceptionHandler(Exception.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleAllException(
                        Exception e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

                log.error("서버 내부 예외 발생",
                        kv("url", request.getRequestURI()),
                        kv("method", request.getMethod()),
                        kv("exceptionName", e.getClass().getSimpleName()),
                        kv("errorMessage", e.getMessage()),
                        e
                );

                ErrorResponse errorResponse = ErrorResponse.of(
                                errorCode,
                                e.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(errorCode.getHttpStatus())
                                .body(CommonResponse.fail(errorResponse));
        }
}
