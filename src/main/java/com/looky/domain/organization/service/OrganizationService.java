package com.looky.domain.organization.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.organization.dto.OrganizationResponse;
import com.looky.domain.organization.dto.CreateOrganizationRequest;
import com.looky.domain.organization.dto.UpdateOrganizationRequest;
import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.entity.OrganizationCategory;
import com.looky.domain.organization.entity.University;
import com.looky.domain.organization.entity.UserOrganization;
import com.looky.domain.organization.repository.OrganizationRepository;
import com.looky.domain.organization.repository.UniversityRepository;
import com.looky.domain.organization.repository.UserOrganizationRepository;
import com.looky.domain.user.entity.CouncilProfile;
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

        public List<OrganizationResponse> getOrganizationsForAll(Long universityId) {
                universityRepository.findById(universityId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학을 찾을 수 없습니다."));

                return organizationRepository.findByUniversityIdOrderByNameAsc(universityId).stream()
                                .map(OrganizationResponse::from)
                                .collect(Collectors.toList());
        }

        public List<OrganizationResponse> getDepartmentsByCollegeForAll(Long collegeId) {
                Organization college = organizationRepository.findById(collegeId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "단과대학을 찾을 수 없습니다."));

                if (college.getCategory() != OrganizationCategory.COLLEGE) {
                        throw new CustomException(ErrorCode.BAD_REQUEST, "단과대학만 조회할 수 있습니다.");
                }

                return organizationRepository.findByParentIdAndCategoryOrderByNameAsc(collegeId, OrganizationCategory.DEPARTMENT)
                                .stream()
                                .map(OrganizationResponse::from)
                                .collect(Collectors.toList());
        }

        // --- 관리자 ---

        @Transactional
        public Long createOrganizationForAdmin(Long universityId, CreateOrganizationRequest request) {
                University university = universityRepository.findById(universityId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학을 찾을 수 없습니다."));

                Organization parent = null;
                if (request.getParentId() != null) {
                        parent = organizationRepository.findById(request.getParentId())
                                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "단과대학을 찾을 수 없습니다."));
                }

                if (organizationRepository.existsByUniversityIdAndName(universityId, request.getName())) {
                        throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 소속 이름입니다.");
                }

                Organization organization = request.toEntity(university, parent, null);
                return organizationRepository.save(organization).getId();
        }

        @Transactional
        public void updateOrganizationForAdmin(Long organizationId, UpdateOrganizationRequest request) {
                Organization organization = organizationRepository.findById(organizationId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "소속을 찾을 수 없습니다."));

                validateOrganizationUpdateRequest(request, organization);

                Organization parent = resolveParent(request, organization);
                checkNameDuplicate(request, organization);
                applyUpdate(organization, request, parent);
        }

        @Transactional
        public void deleteOrganizationForAdmin(Long organizationId) {
                Organization organization = organizationRepository.findById(organizationId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "소속을 찾을 수 없습니다."));

                if (organizationRepository.existsByParentId(organizationId)) {
                        throw new CustomException(ErrorCode.BAD_REQUEST, "하위 조직이 있어 삭제할 수 없습니다.");
                }

                organizationRepository.delete(organization);
        }

        // --- 학생회 ---

        @Transactional
        public Long createOrganizationForCouncil(User user, Long universityId, CreateOrganizationRequest request) {
                University university = universityRepository.findById(universityId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학을 찾을 수 없습니다."));

                CouncilProfile councilProfile = councilProfileRepository.findById(user.getId())
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "학생회를 찾을 수 없습니다."));
                if (!Objects.equals(councilProfile.getUniversity().getId(), universityId)) {
                        throw new CustomException(ErrorCode.FORBIDDEN, "자신의 대학에만 소속을 등록할 수 있습니다.");
                }

                Organization parent = null;
                if (request.getParentId() != null) {
                        parent = organizationRepository.findById(request.getParentId())
                                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "단과대학을 찾을 수 없습니다."));
                }

                if (organizationRepository.existsByUniversityIdAndName(universityId, request.getName())) {
                        throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 소속 이름입니다.");
                }

                Organization organization = request.toEntity(university, parent, user);
                return organizationRepository.save(organization).getId();
        }

        @Transactional
        public void updateOrganizationForCouncil(Long organizationId, User user, UpdateOrganizationRequest request) {
                Organization organization = organizationRepository.findById(organizationId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "소속을 찾을 수 없습니다."));

                if (!Objects.equals(organization.getUser().getId(), user.getId())) {
                        throw new CustomException(ErrorCode.FORBIDDEN, "본인이 생성한 소속만 수정할 수 있습니다.");
                }

                validateOrganizationUpdateRequest(request, organization);

                Organization parent = resolveParent(request, organization);
                checkNameDuplicate(request, organization);
                applyUpdate(organization, request, parent);
        }

        @Transactional
        public void deleteOrganizationForCouncil(Long organizationId, User user) {
                Organization organization = organizationRepository.findById(organizationId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "소속을 찾을 수 없습니다."));

                if (!Objects.equals(organization.getUser().getId(), user.getId())) {
                        throw new CustomException(ErrorCode.FORBIDDEN, "본인이 생성한 소속만 삭제할 수 있습니다.");
                }

                if (organizationRepository.existsByParentId(organizationId)) {
                        throw new CustomException(ErrorCode.BAD_REQUEST, "하위 조직이 있어 삭제할 수 없습니다.");
                }

                organizationRepository.delete(organization);
        }

        // --- 학생 ---

        @Transactional
        public void joinOrganizationForStudent(Long organizationId, User user) {
                Organization organization = organizationRepository.findById(organizationId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "소속을 찾을 수 없습니다."));

                User currentUser = userRepository.findById(user.getId())
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                if (userOrganizationRepository.existsByUserAndOrganization(currentUser, organization)) {
                        throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 가입된 소속입니다.");
                }

                if (userOrganizationRepository.existsByUserAndOrganization_Category(currentUser, organization.getCategory())) {
                        String categoryName;
                        if (organization.getCategory() == OrganizationCategory.COLLEGE) categoryName = "단과대학";
                        else if (organization.getCategory() == OrganizationCategory.DEPARTMENT) categoryName = "학과";
                        else if (organization.getCategory() == OrganizationCategory.UNIVERSITY_COUNCIL) categoryName = "총학생회";
                        else categoryName = "총동아리연합회";

                        throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 가입한 " + categoryName + "이 있습니다.");
                }

                if (organization.getCategory().equals(OrganizationCategory.DEPARTMENT)) {
                        UserOrganization collegeMembership = userOrganizationRepository
                                        .findByUserAndOrganizationCategory(currentUser, OrganizationCategory.COLLEGE)
                                        .stream().findFirst()
                                        .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST, "학과에 가입하려면 먼저 단과대학에 소속되어야 합니다."));

                        if (!organization.getParent().getId().equals(collegeMembership.getOrganization().getId())) {
                                throw new CustomException(ErrorCode.BAD_REQUEST, "선택한 학과가 현재 소속된 단과대학에 속하지 않습니다.");
                        }
                }

                UserOrganization userOrganization = UserOrganization.builder()
                                .user(currentUser)
                                .organization(organization)
                                .build();

                userOrganizationRepository.save(userOrganization);
        }

        @Transactional
        public void leaveOrganizationForStudent(Long organizationId, User user) {
                Organization organization = organizationRepository.findById(organizationId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "소속을 찾을 수 없습니다."));

                User currentUser = userRepository.findById(user.getId())
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                UserOrganization userOrganization = userOrganizationRepository
                                .findByUserAndOrganization(currentUser, organization)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가입되지 않은 소속입니다."));

                userOrganizationRepository.delete(userOrganization);
        }

        @Transactional
        public void changeOrganizationForStudent(Long organizationId, User user) {
                Organization newOrganization = organizationRepository.findById(organizationId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "소속을 찾을 수 없습니다."));

                User currentUser = userRepository.findById(user.getId())
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                if (userOrganizationRepository.existsByUserAndOrganization_Category(currentUser, newOrganization.getCategory())) {
                        userOrganizationRepository.deleteByUserAndOrganizationCategory(currentUser, newOrganization.getCategory());
                }

                if (newOrganization.getCategory() == OrganizationCategory.COLLEGE) {
                        if (userOrganizationRepository.existsByUserAndOrganization_Category(currentUser, OrganizationCategory.DEPARTMENT)) {
                                userOrganizationRepository.deleteByUserAndOrganizationCategory(currentUser, OrganizationCategory.DEPARTMENT);
                        }
                }

                joinOrganizationForStudent(organizationId, user);
        }

        // --- private helpers ---

        private void validateOrganizationUpdateRequest(UpdateOrganizationRequest request, Organization organization) {
                if (request.getCategory().isPresent() && request.getCategory().get() == null) {
                        throw new CustomException(ErrorCode.BAD_REQUEST, "카테고리는 필수입니다.");
                }
                if (request.getName().isPresent() && (request.getName().get() == null || request.getName().get().isBlank())) {
                        throw new CustomException(ErrorCode.BAD_REQUEST, "소속 이름은 필수입니다.");
                }
        }

        private Organization resolveParent(UpdateOrganizationRequest request, Organization organization) {
                if (request.getParentId().isPresent()) {
                        Long newParentId = request.getParentId().get();
                        if (newParentId == null) return null;
                        return organizationRepository.findById(newParentId)
                                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "단과대학을 찾을 수 없습니다."));
                }
                return organization.getParent();
        }

        private void checkNameDuplicate(UpdateOrganizationRequest request, Organization organization) {
                if (request.getName().isPresent()) {
                        String newName = request.getName().get();
                        if (!organization.getName().equals(newName) &&
                                        organizationRepository.existsByUniversityIdAndName(organization.getUniversity().getId(), newName)) {
                                throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 소속 이름입니다.");
                        }
                }
        }

        private void applyUpdate(Organization organization, UpdateOrganizationRequest request, Organization parent) {
                organization.update(
                        request.getCategory().orElse(organization.getCategory()),
                        request.getName().orElse(organization.getName()),
                        request.getExpiresAt().orElse(organization.getExpiresAt()),
                        parent
                );
        }
}
