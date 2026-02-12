package com.looky.domain.admin.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.admin.dto.StoreClaimResponse;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreClaim;
import com.looky.domain.store.entity.StoreClaimStatus;
import com.looky.domain.store.repository.StoreClaimRepository;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.user.entity.OwnerProfile;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.OwnerProfileRepository;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.looky.domain.store.service.StoreService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreClaimVerificationService {

    private final StoreClaimRepository storeClaimRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final StoreService storeService;

    public Page<StoreClaimResponse> getStoreClaims(StoreClaimStatus status, Pageable pageable) {
        Page<StoreClaim> claims;
        if (status != null) {
            claims = storeClaimRepository.findByStatus(status, pageable);
        } else {
            claims = storeClaimRepository.findAll(pageable);
        }
        
        return claims.map(claim -> {
            String ownerName = ownerProfileRepository.findById(claim.getUserId())
                    .map(OwnerProfile::getName)
                    .orElse("Unknown");

            return StoreClaimResponse.from(claim, ownerName);
        });
    }

    @Transactional
    public void approve(Long claimId) {
        StoreClaim claim = storeClaimRepository.findById(claimId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 점유 요청입니다."));

        if (claim.getStatus() != StoreClaimStatus.PENDING) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "대기 중인 요청만 승인할 수 있습니다.");
        }

        Store store = storeRepository.findById(claim.getStoreId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 상점입니다."));

        User user = userRepository.findById(claim.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 승인 처리
        claim.updateStatus(StoreClaimStatus.APPROVED, null);

        // 연관 스토어 정보 업데이트
        store.approveClaim(user, claim.getBizRegNo(), claim.getStorePhone(), claim.getRepresentativeName());
        
        // 등급 재계산
        storeService.recalculateCloverGrade(store);
    }

    @Transactional
    public void reject(Long claimId, String reason) {
        StoreClaim claim = storeClaimRepository.findById(claimId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 점유 요청입니다."));

        if (claim.getStatus() != StoreClaimStatus.PENDING) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "대기 중인 요청만 반려할 수 있습니다.");
        }

        claim.updateStatus(StoreClaimStatus.REJECTED, reason);
    }
    
    @Transactional
    public void updateMemo(Long claimId, String memo) {
        StoreClaim claim = storeClaimRepository.findById(claimId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 점유 요청입니다."));
        
        claim.updateAdminMemo(memo);
    }
}
