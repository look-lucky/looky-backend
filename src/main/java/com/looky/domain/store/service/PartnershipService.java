package com.looky.domain.store.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.organization.repository.OrganizationRepository;
import com.looky.domain.store.dto.CreatePartnershipRequest;
import com.looky.domain.store.dto.UpdatePartnershipRequest;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreOrganization;
import com.looky.domain.store.repository.StoreOrganizationRepository;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PartnershipService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreOrganizationRepository storeOrganizationRepository;

    // 제휴 등록
    @Transactional
    public Long createPartnership(Long storeId, User user, CreatePartnershipRequest request) {
        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        // 본인 소유 확인
        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 소유의 가게가 아닙니다.");
        }

        com.looky.domain.organization.entity.Organization organization = organizationRepository
                .findById(request.getOrganizationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "조직을 찾을 수 없습니다."));

        if (storeOrganizationRepository.existsByStoreIdAndOrganizationId(storeId, request.getOrganizationId())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 등록된 제휴입니다.");
        }

        com.looky.domain.store.entity.StoreOrganization storeOrganization = com.looky.domain.store.entity.StoreOrganization
                .builder()
                .store(store)
                .organization(organization)
                .benefit(request.getBenefit())
                .build();

        storeOrganizationRepository.save(storeOrganization);

        return storeOrganization.getId();
    }

    // 제휴 수정
    @Transactional
    public void updatePartnershipBenefit(Long storeId, Long partnershipId, User user, UpdatePartnershipRequest request) {

        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        // 본인 소유 확인
        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 소유의 가게가 아닙니다.");
        }

        com.looky.domain.store.entity.StoreOrganization storeOrganization = storeOrganizationRepository
                .findById(partnershipId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "제휴 정보를 찾을 수 없습니다."));

        if (!storeOrganization.getStore().getId().equals(storeId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "해당 상점의 제휴 정보가 아닙니다.");
        }

        storeOrganization.updateBenefit(request.getBenefit());
    }

    // 제휴 삭제
    @Transactional
    public void deletePartnership(Long storeId, Long partnershipId, User user) {

        User owner = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        // 본인 소유 확인
        if (!Objects.equals(store.getUser().getId(), owner.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 소유의 가게가 아닙니다.");
        }

        StoreOrganization storeOrganization = storeOrganizationRepository.findById(partnershipId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "제휴 정보를 찾을 수 없습니다."));

        if (!storeOrganization.getStore().getId().equals(storeId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "해당 상점의 제휴 정보가 아닙니다.");
        }

        storeOrganizationRepository.delete(storeOrganization);
    }
}
