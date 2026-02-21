package com.looky.domain.partnership.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.organization.entity.UserOrganization;
import com.looky.domain.organization.repository.UserOrganizationRepository;
import com.looky.domain.partnership.dto.StorePartnershipResponse;
import com.looky.domain.partnership.entity.Partnership;
import com.looky.domain.partnership.repository.PartnershipRepository;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.user.entity.Role;
import com.looky.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnershipService {

    private final PartnershipRepository partnershipRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final StoreRepository storeRepository;

    public List<StorePartnershipResponse> getStorePartnerships(Long storeId, User user) {
        if (!storeRepository.existsById(storeId)) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다.");
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 1. 해당 상점의 모든 활성 제휴 조회 (대학 상관없이)
        List<Partnership> partnerships = partnershipRepository.findAllActiveByStoreId(storeId, today);
        
        // 2. 로그인한 학생 유저라면, 소속 조직 ID 목록 가져오기
        Set<Long> myOrganizationIds = new HashSet<>();
        if (user != null && user.getRole() == Role.ROLE_STUDENT) {
             List<UserOrganization> userOrganizations = userOrganizationRepository.findAllByUser(user);
             myOrganizationIds = userOrganizations.stream()
                     .map(uo -> uo.getOrganization().getId())
                     .collect(Collectors.toSet());
        }

        Set<Long> finalMyOrgIds = myOrganizationIds;
        
        return partnerships.stream()
                .map(p -> StorePartnershipResponse.of(
                        p.getOrganization().getName(),
                        p.getBenefit(),
                        finalMyOrgIds.contains(p.getOrganization().getId())
                ))
                .toList();
    }

    // 상점 리스트에서 상점 제휴-내 소속 매칭 조직 이름 목록 반환
    public Map<Long, List<String>> getMyPartnershipOrganizations(List<Long> storeIds, User user) {
         Map<Long, List<String>> result = new HashMap<>();
         if (storeIds.isEmpty() || user == null || user.getRole() != Role.ROLE_STUDENT) {
             return result; // 로그인 안했거나 학생이 아니거나 상점이 없으면 빈 맵 반환
         }

         // 1. 내 소속 조직 ID들 추출 (추가로 Organization 객체까지 fetch 되어야 이름 알 수 있음)
         List<UserOrganization> userOrganizations = userOrganizationRepository.findAllByUser(user);
         if (userOrganizations.isEmpty()) {
             return result;
         }

         List<Long> myOrganizationIds = userOrganizations.stream()
                 .map(uo -> uo.getOrganization().getId())
                 .toList();

         LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

         // 2. IN 쿼리로 storeIds에 속하면서 내 조직ID에도 속하는 현재 유효한 제휴들을 모두 가져오기
         List<Partnership> matchingPartnerships = partnershipRepository.findActivePartnershipsByStoreIdsAndOrganizationIds(storeIds, myOrganizationIds, today);

         // 3. storeId 기준으로 그룹핑 및 조직 이름 String 리스트로 치환하여 Map 생성
         return matchingPartnerships.stream()
                 .collect(Collectors.groupingBy(
                         p -> p.getStore().getId(),
                         Collectors.mapping(p -> p.getOrganization().getName(), Collectors.toList())
                 ));
    }
}
