package com.looky.domain.store.controller;

import com.looky.common.response.CommonResponse;
import com.looky.domain.store.dto.BizVerificationRequest;
import com.looky.domain.store.dto.BizVerificationResponse;
import com.looky.domain.store.dto.StoreResponse;
import com.looky.domain.store.dto.StoreClaimRequest;
import com.looky.domain.store.dto.MyStoreClaimResponse;
import com.looky.domain.store.service.StoreClaimService;
import com.looky.security.details.PrincipalDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "StoreClaim", description = "상점 소유권 등록 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class StoreClaimController {

    private final StoreClaimService storeClaimService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(summary = "[점주] 미등록 상점 검색", description = "시스템에 등록된 미등록 상점을 이름 또는 주소로 검색합니다.")
    @GetMapping("/store-claims/search")
    public ResponseEntity<CommonResponse<List<StoreResponse>>> searchUnclaimedStores(
            @RequestParam String keyword
    ) {
        log.debug("[StoreClaimController] searchUnclaimedStores 요청 - keyword: {}", keyword);
        List<StoreResponse> response = storeClaimService.searchUnclaimedStores(keyword);
        log.debug("[StoreClaimController] searchUnclaimedStores 응답 - 결과 수: {}, 데이터: {}", response.size(), response);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "[점주] 사업자등록번호 유효성 검증", description = "사업자등록번호의 유효성을 검증합니다.")
    @PostMapping("/biz-reg-no/verify")
    public ResponseEntity<CommonResponse<BizVerificationResponse>> verifyBizRegNo(@RequestBody @Valid BizVerificationRequest request) {
        BizVerificationResponse response = storeClaimService.verifyBizRegNo(request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }


    @Operation(summary = "[점주] 상점 소유 요청 등록", description = "사장님이 상점에 대해 소유를 요청하여 심사 대상이 됩니다.")
    @PostMapping("/store-claims")
    public ResponseEntity<CommonResponse<Long>> createStoreClaims(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestPart("request") String requestJson,
            @Parameter(description = "사업자등록증 이미지") @RequestPart MultipartFile image
    ) throws IOException, MethodArgumentNotValidException {
        StoreClaimRequest request = objectMapper.readValue(requestJson, StoreClaimRequest.class);
        validateRequest(request);

        Long storeClaimId = storeClaimService.createStoreClaims(principalDetails.getUser(), request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(storeClaimId));
    }

    @Operation(summary = "[점주] 내 상점 소유 요청 목록 조회", description = "점주가 자신이 신청한 상점 소유 요청 목록을 조회합니다.")
    @GetMapping("/store-claims/my")
    public ResponseEntity<CommonResponse<List<MyStoreClaimResponse>>> getMyStoreClaims(
            @Parameter(hidden = true) @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        List<MyStoreClaimResponse> response = storeClaimService.getMyStoreClaims(principalDetails.getUser());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    private <T> void validateRequest(T request) throws MethodArgumentNotValidException {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            BindingResult bindingResult = new BeanPropertyBindingResult(request, request.getClass().getName());
            for (ConstraintViolation<T> violation : violations) {
                bindingResult.addError(new FieldError(
                        request.getClass().getName(),
                        violation.getPropertyPath().toString(),
                        violation.getInvalidValue(),
                        false,
                        null,
                        null,
                        violation.getMessage()
                ));
            }
            try {
                MethodParameter parameter = new MethodParameter(
                        this.getClass().getDeclaredMethod("validateRequest", Object.class), 0);
                throw new MethodArgumentNotValidException(parameter, bindingResult);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
