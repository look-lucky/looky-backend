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
import com.looky.domain.user.dto.ChangePasswordRequest;
import com.looky.domain.user.dto.ChangeUsernameRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.looky.domain.user.dto.StudentInfoResponse;
import com.looky.domain.user.dto.OwnerInfoResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final UniversityRepository universityRepository;

    // 아이디 변경
    @Transactional
    public void changeUsername(Long userId, ChangeUsernameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (userRepository.existsByUsername(request.getNewUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 아이디입니다.");
        }

        user.updateUsername(request.getNewUsername());
    }

    // 비밀번호 재설정 (비밀번호 변경)
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이전 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    // 학생 프로필 조회
    public StudentInfoResponse getStudentInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.ROLE_STUDENT) {
            throw new CustomException(ErrorCode.FORBIDDEN, "학생 회원만 이용 가능합니다.");
        }

        StudentProfile profile = studentProfileRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "학생 프로필을 찾을 수 없습니다."));

        Long collegeId = null;
        Long departmentId = null;

        List<UserOrganization> userOrgs = userOrganizationRepository.findAllByUser(user);
        for (UserOrganization uo : userOrgs) {
            if (uo.getOrganization().getCategory() == OrganizationCategory.COLLEGE) {
                collegeId = uo.getOrganization().getId();
            } else if (uo.getOrganization().getCategory() == OrganizationCategory.DEPARTMENT) {
                departmentId = uo.getOrganization().getId();
            }
        }

        return StudentInfoResponse.builder()
                .universityId(profile.getUniversity() != null ? profile.getUniversity().getId() : null)
                .collegeId(collegeId)
                .departmentId(departmentId)
                .isClubMember(profile.getIsClubMember())
                .build();
    }

    // 점주 프로필 조회
    public OwnerInfoResponse getOwnerInfo(Long userId) {
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


    // 학생 프로필 수정
    @Transactional
    public void updateStudentProfile(Long userId, UpdateStudentProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.ROLE_STUDENT) {
            throw new CustomException(ErrorCode.FORBIDDEN, "학생 회원만 이용 가능합니다.");
        }

        StudentProfile profile = studentProfileRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "학생 프로필을 찾을 수 없습니다."));

        // 단과대 업데이트
        if (request.getCollegeId() != null) {
            updateUserOrganization(user, request.getCollegeId(), OrganizationCategory.COLLEGE);
        }

        // 학과 업데이트
        if (request.getDepartmentId() != null) {
            updateUserOrganization(user, request.getDepartmentId(), OrganizationCategory.DEPARTMENT);
        }

        profile.update(request.getNickname(), request.getIsClubMember() != null ? request.getIsClubMember() : profile.getIsClubMember(), null);
    }

    // 대학 수정
    @Transactional
    public void updateUniversity(Long userId, UpdateUniversityRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.ROLE_STUDENT) {
            throw new CustomException(ErrorCode.FORBIDDEN, "학생 회원만 이용 가능합니다.");
        }

        University university = universityRepository.findById(request.getUniversityId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 대학을 찾을 수 없습니다."));

        StudentProfile profile = studentProfileRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "학생 프로필을 찾을 수 없습니다."));
        
        // 대학 변경 시 기존 단과대/학과 정보 삭제 (대학이 바뀌었으므로 불일치 방지)
        userOrganizationRepository.deleteByUserAndOrganizationCategory(user, OrganizationCategory.COLLEGE);
        userOrganizationRepository.deleteByUserAndOrganizationCategory(user, OrganizationCategory.DEPARTMENT);

        profile.update(null, profile.getIsClubMember(), university);
    }

    // 사용자 조직 수정
    private void updateUserOrganization(User user, Long organizationId, OrganizationCategory category) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 조직을 찾을 수 없습니다."));

        if (organization.getCategory() != category) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "조직 카테고리가 일치하지 않습니다.");
        }

        // 기존 해당 카테고리 조직 삭제 (단과대/학과는 보통 1개씩만 소속된다고 가정)
        userOrganizationRepository.deleteByUserAndOrganizationCategory(user, category);

        // 새 조직 추가
        UserOrganization userOrg = UserOrganization.builder()
                .user(user)
                .organization(organization)
                .build();
        userOrganizationRepository.save(userOrg);
    }
}
