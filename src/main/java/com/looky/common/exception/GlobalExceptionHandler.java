package com.looky.common.exception;

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

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        // 일반 예러
        @ExceptionHandler(CustomException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleCustomException(CustomException e,
                        HttpServletRequest request) {
                ErrorCode errorCode = e.getErrorCode();

                log.warn("[CustomException] url: {} | errorType: {} | message: {}", request.getRequestURI(),
                                errorCode.name(), e.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(
                                errorCode,
                                e.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(errorCode.getHttpStatus())
                                .body(CommonResponse.fail(errorResponse));
        }

        // 검증 에러
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleValidationException(
                        MethodArgumentNotValidException e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

                log.info("[ValidationException] url: {} | message: {}", request.getRequestURI(), e.getMessage());

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

                log.info("[AuthenticationException] url: {} | message: {}", request.getRequestURI(), e.getMessage());

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

                log.info("[AccessDeniedException] url: {} | message: {}", request.getRequestURI(), e.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(
                                errorCode,
                                e.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(errorCode.getHttpStatus())
                                .body(CommonResponse.fail(errorResponse));
        }

        // 405 에러
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleMethodNotSupportedException(
                        HttpRequestMethodNotSupportedException e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;

                log.info("[MethodNotAllowed] url: {} | message: {}", request.getRequestURI(), e.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(errorCode, e.getMessage(), request.getRequestURI());

                return ResponseEntity.status(errorCode.getHttpStatus()).body(CommonResponse.fail(errorResponse));
        }

        // 415 에러
        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleMediaTypeNotSupportedException(
                        HttpMediaTypeNotSupportedException e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.UNSUPPORTED_MEDIA_TYPE;

                log.info("[UnsupportedMediaType] url: {} | message: {}", request.getRequestURI(), e.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(errorCode, e.getMessage(), request.getRequestURI());

                return ResponseEntity.status(errorCode.getHttpStatus()).body(CommonResponse.fail(errorResponse));
        }

        // JSON 파싱 에러 (예: Enum 값 불일치)
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleHttpMessageNotReadableException(
                        HttpMessageNotReadableException e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.BAD_REQUEST;

                log.info("[HttpMessageNotReadable] url: {} | message: {}", request.getRequestURI(), e.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(errorCode, "잘못된 요청 데이터 형식입니다. (JSON 파싱 오류)", request.getRequestURI());

                return ResponseEntity.status(errorCode.getHttpStatus()).body(CommonResponse.fail(errorResponse));
        }

        // 파라미터 타입 불일치 (예: Long 타입 파라미터에 문자열 입력)
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleMethodArgumentTypeMismatchException(
                        MethodArgumentTypeMismatchException e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.BAD_REQUEST;

                log.info("[MethodArgumentTypeMismatch] url: {} | message: {}", request.getRequestURI(), e.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(errorCode, String.format("잘못된 파라미터 값입니다. (%s)", e.getName()), request.getRequestURI());

                return ResponseEntity.status(errorCode.getHttpStatus()).body(CommonResponse.fail(errorResponse));
        }

        // 나머지 모든 예외 처리
        @ExceptionHandler(Exception.class)
        public ResponseEntity<CommonResponse<ErrorResponse>> handleAllException(
                        Exception e,
                        HttpServletRequest request) {

                ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

                log.warn("[InternalServerError] url: {} | message: {}", request.getRequestURI(), e.getMessage(), e);

                ErrorResponse errorResponse = ErrorResponse.of(
                                errorCode,
                                e.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(errorCode.getHttpStatus())
                                .body(CommonResponse.fail(errorResponse));
        }
}
