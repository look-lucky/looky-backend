package com.looky.domain.organization.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.organization.dto.OrganizationResponse;
import com.looky.domain.organization.dto.CreateOrganizationRequest;
import com.looky.domain.organization.dto.UpdateOrganizationRequest;
import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.entity.University;
import com.looky.domain.organization.entity.UserOrganization;
import com.looky.domain.organization.repository.OrganizationRepository;
import com.looky.domain.organization.repository.UniversityRepository;
import com.looky.domain.organization.repository.UserOrganizationRepository;
import com.looky.domain.user.entity.CouncilProfile;
import com.looky.domain.user.entity.Role;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.CouncilProfileRepository;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

        private final OrganizationRepository organizationRepository;
        private final UniversityRepository universityRepository;
        private final UserOrganizationRepository userOrganizationRepository;
        private final UserRepository userRepository;
        private final CouncilProfileRepository councilProfileRepository;

        // --- 공통 ---
        public List<OrganizationResponse> getOrganizations(Long universityId) {
                University university = universityRepository.findById(universityId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학을 찾을 수 없습니다."));

                return organizationRepository.findAll().stream()
                                .filter(a -> a.getUniversity().getId().equals(universityId))
                                .map(OrganizationResponse::from)
                                .collect(Collectors.toList());
        }

        // --- 관리자 ---

        @Transactional
        public Long createOrganization(User user, Long universityId, CreateOrganizationRequest request) {
                University university = universityRepository.findById(universityId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학을 찾을 수 없습니다."));

                Organization parent = null;

                // 학생회가 등록할 때
                if (user.getRole() == Role.ROLE_COUNCIL) {
                        CouncilProfile councilProfile = councilProfileRepository.findById(user.getId())
                                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "학생회를 찾을 수 없습니다."));
                        if (!Objects.equals(councilProfile.getUniversity().getId(), universityId)) {
                                throw new CustomException(ErrorCode.FORBIDDEN, "자신의 대학에만 소속을 등록할 수 있습니다.");
                        }
                }

                // 학과일 때
                if (request.getParentId() != null) {
                        parent = organizationRepository.findById(request.getParentId())
                                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "단과대학을 찾을 수 없습니다."));
                }

                Organization organization = request.toEntity(university, parent, user);
                Organization savedOrganization = organizationRepository.save(organization);
                return savedOrganization.getId();
        }

        @Transactional
        public void updateOrganization(Long organizationId, User user, UpdateOrganizationRequest request) {
                Organization organization = organizationRepository.findById(organizationId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "소속을 찾을 수 없습니다."));

                // 본인 소유 확인
                if (!Objects.equals(organization.getUser().getId(), user.getId())) {
                        throw new CustomException(ErrorCode.FORBIDDEN, "본인이 생성한 소속만 수정할 수 있습니다.");
                }

                Organization parent = null;

                // 학과일 때
                if (request.getParentId() != null) {
                        parent = organizationRepository.findById(request.getParentId())
                                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "단과대학을 찾을 수 없습니다."));
                }

                organization.update(request.getCategory(), request.getName(), request.getExpiresAt(), parent);
        }

        @Transactional
        public void deleteOrganization(Long organizationId, User user) {
                Organization organization = organizationRepository.findById(organizationId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "소속을 찾을 수 없습니다."));

                // 본인 소유 확인
                if (!Objects.equals(organization.getUser().getId(), user.getId())) {
                        throw new CustomException(ErrorCode.FORBIDDEN, "본인이 생성한 소속만 삭제할 수 있습니다.");
                }

                organizationRepository.delete(organization);
        }

        // --- 학생 ---

        @Transactional
        public void joinOrganization(Long organizationId, User user) {
                Organization organization = organizationRepository.findById(organizationId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "소속을 찾을 수 없습니다."));

                User currentUser = userRepository.findById(user.getId())
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                if (userOrganizationRepository.existsByUserAndOrganization(currentUser, organization)) {
                        throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 가입된 소속입니다.");
                }

                UserOrganization userOrganization = UserOrganization.builder()
                                .user(currentUser)
                                .organization(organization)
                                .build();

                userOrganizationRepository.save(userOrganization);
        }

        @Transactional
        public void leaveOrganization(Long organizationId, User user) {
                Organization organization = organizationRepository.findById(organizationId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "소속을 찾을 수 없습니다."));

                User currentUser = userRepository.findById(user.getId())
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                UserOrganization userOrganization = userOrganizationRepository
                                .findByUserAndOrganization(currentUser, organization)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가입되지 않은 소속입니다."));

                userOrganizationRepository.delete(userOrganization);
        }
}
