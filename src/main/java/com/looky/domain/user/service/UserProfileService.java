package com.looky.domain.user.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.entity.OrganizationCategory;
import com.looky.domain.organization.entity.University;
import com.looky.domain.organization.entity.UserOrganization;
import com.looky.domain.organization.repository.OrganizationRepository;
import com.looky.domain.organization.repository.UniversityRepository;
import com.looky.domain.organization.repository.UserOrganizationRepository;
import com.looky.domain.user.dto.OwnerInfoResponse;
import com.looky.domain.user.dto.StudentInfoResponse;
import com.looky.domain.user.dto.UpdateStudentProfileRequest;
import com.looky.domain.user.dto.UpdateUniversityRequest;
import com.looky.domain.user.entity.OwnerProfile;
import com.looky.domain.user.entity.Role;
import com.looky.domain.user.entity.StudentProfile;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.OwnerProfileRepository;
import com.looky.domain.user.repository.StudentProfileRepository;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final OrganizationRepository organizationRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final UniversityRepository universityRepository;

    public StudentInfoResponse getStudentInfoForStudent(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.ROLE_STUDENT) {
            throw new CustomException(ErrorCode.FORBIDDEN, "학생 회원만 이용 가능합니다.");
        }

        StudentProfile profile = studentProfileRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "학생 프로필을 찾을 수 없습니다."));

        Long collegeId = null;
        Long departmentId = null;
        String collegeName = null;
        String departmentName = null;

        List<UserOrganization> userOrgs = userOrganizationRepository.findAllByUser(user);
        for (UserOrganization uo : userOrgs) {
            if (uo.getOrganization().getCategory() == OrganizationCategory.COLLEGE) {
                collegeId = uo.getOrganization().getId();
                collegeName = uo.getOrganization().getName();
            } else if (uo.getOrganization().getCategory() == OrganizationCategory.DEPARTMENT) {
                departmentId = uo.getOrganization().getId();
                departmentName = uo.getOrganization().getName();
            }
        }

        return StudentInfoResponse.builder()
                .username(user.getUsername())
                .nickname(profile.getNickname())
                .universityId(profile.getUniversity() != null ? profile.getUniversity().getId() : null)
                .collegeId(collegeId)
                .departmentId(departmentId)
                .universityName(profile.getUniversity() != null ? profile.getUniversity().getName() : null)
                .collegeName(collegeName)
                .departmentName(departmentName)
                .isClubMember(profile.getIsClubMember())
                .build();
    }

    public OwnerInfoResponse getOwnerInfoForOwner(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.ROLE_OWNER) {
            throw new CustomException(ErrorCode.FORBIDDEN, "점주 회원만 이용 가능합니다.");
        }

        OwnerProfile profile = ownerProfileRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "점주 프로필을 찾을 수 없습니다."));

        return OwnerInfoResponse.builder()
                .name(profile.getName())
                .email(user.getEmail())
                .username(user.getUsername())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .build();
    }

    @Transactional
    public void updateStudentProfileForStudent(Long userId, UpdateStudentProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.ROLE_STUDENT) {
            throw new CustomException(ErrorCode.FORBIDDEN, "학생 회원만 이용 가능합니다.");
        }

        StudentProfile profile = studentProfileRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "학생 프로필을 찾을 수 없습니다."));

        if (request.getCollegeId() != null) {
            updateUserOrganization(user, request.getCollegeId(), OrganizationCategory.COLLEGE);
        }

        if (request.getDepartmentId() != null) {
            updateUserOrganization(user, request.getDepartmentId(), OrganizationCategory.DEPARTMENT);
        }

        profile.update(request.getNickname(), request.getIsClubMember() != null ? request.getIsClubMember() : profile.getIsClubMember(), null);
    }

    @Transactional
    public void updateUniversityForStudent(Long userId, UpdateUniversityRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.ROLE_STUDENT) {
            throw new CustomException(ErrorCode.FORBIDDEN, "학생 회원만 이용 가능합니다.");
        }

        University university = universityRepository.findById(request.getUniversityId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 대학을 찾을 수 없습니다."));

        StudentProfile profile = studentProfileRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "학생 프로필을 찾을 수 없습니다."));

        userOrganizationRepository.deleteByUserAndOrganizationCategory(user, OrganizationCategory.COLLEGE);
        userOrganizationRepository.deleteByUserAndOrganizationCategory(user, OrganizationCategory.DEPARTMENT);

        profile.update(null, profile.getIsClubMember(), university);
    }

    private void updateUserOrganization(User user, Long organizationId, OrganizationCategory category) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 조직을 찾을 수 없습니다."));

        if (organization.getCategory() != category) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "조직 카테고리가 일치하지 않습니다.");
        }

        userOrganizationRepository.deleteByUserAndOrganizationCategory(user, category);

        UserOrganization userOrg = UserOrganization.builder()
                .user(user)
                .organization(organization)
                .build();
        userOrganizationRepository.save(userOrg);
    }
}
