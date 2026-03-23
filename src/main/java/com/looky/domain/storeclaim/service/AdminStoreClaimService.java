package com.looky.domain.storeclaim.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.store.service.StoreService;
import com.looky.domain.storeclaim.dto.AdminStoreClaimResponse;
import com.looky.domain.storeclaim.entity.StoreClaim;
import com.looky.domain.storeclaim.entity.StoreClaimStatus;
import com.looky.domain.storeclaim.repository.StoreClaimRepository;
import com.looky.domain.user.entity.OwnerProfile;
import com.looky.domain.user.repository.OwnerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStoreClaimService {

    private final StoreClaimRepository storeClaimRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final StoreService storeService;

    public Page<AdminStoreClaimResponse> getStoreClaimsForAdmin(StoreClaimStatus status, Pageable pageable) {
        Page<StoreClaim> claims = (status != null)
                ? storeClaimRepository.findByStatus(status, pageable)
                : storeClaimRepository.findAll(pageable);

        return claims.map(claim -> {
            String ownerName = ownerProfileRepository.findById(claim.getUser().getId())
                    .map(OwnerProfile::getName)
                    .orElse("Unknown");
            return AdminStoreClaimResponse.from(claim, ownerName);
        });
    }

    @Transactional
    public void approveForAdmin(Long claimId) {
        StoreClaim claim = storeClaimRepository.findById(claimId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 점유 요청입니다."));

        if (claim.getStatus() != StoreClaimStatus.PENDING) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "대기 중인 요청만 승인할 수 있습니다.");
        }

        claim.updateStatus(StoreClaimStatus.APPROVED, null);
        claim.getStore().approveClaim(claim.getUser(), claim.getBizRegNo(), claim.getStorePhone(), claim.getRepresentativeName());
        storeService.recalculateCloverGrade(claim.getStore());
    }

    @Transactional
    public void rejectForAdmin(Long claimId, String reason) {
        StoreClaim claim = storeClaimRepository.findById(claimId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 점유 요청입니다."));

        if (claim.getStatus() != StoreClaimStatus.PENDING) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "대기 중인 요청만 반려할 수 있습니다.");
        }

        claim.updateStatus(StoreClaimStatus.REJECTED, reason);
    }

    @Transactional
    public void updateMemoForAdmin(Long claimId, String memo) {
        StoreClaim claim = storeClaimRepository.findById(claimId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 점유 요청입니다."));

        claim.updateAdminMemo(memo);
    }
}
