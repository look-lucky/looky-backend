package com.looky.domain.user.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.entity.University;
import com.looky.domain.organization.entity.UserOrganization;
import com.looky.domain.organization.repository.OrganizationRepository;
import com.looky.domain.organization.repository.UniversityRepository;
import com.looky.domain.organization.repository.UserOrganizationRepository;
import com.looky.domain.user.dto.*;
import com.looky.domain.user.entity.*;
import com.looky.domain.user.repository.CouncilProfileRepository;
import com.looky.domain.user.repository.OwnerProfileRepository;
import com.looky.domain.user.repository.StudentProfileRepository;
import com.looky.domain.user.repository.UserRepository;
import com.looky.domain.user.repository.WithdrawalFeedbackRepository;
import com.looky.security.details.PrincipalDetails;
import com.looky.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final OwnerProfileRepository ownerProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CouncilProfileRepository councilProfileRepository;
    private final UniversityRepository universityRepository;
    private final OrganizationRepository organizationRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final WithdrawalFeedbackRepository withdrawalFeedbackRepository;
    private final EmailVerificationService emailVerificationService;



    // 계정 찾기용 인증번호 발송 (가입된 이메일인지 확인)
    @Transactional
    public void sendVerificationCodeForAccountRecovery(String email) {
        
        // 이메일 존재 여부 확인 (User 테이블)
        if (!userRepository.existsByEmail(email)) {
             throw new CustomException(ErrorCode.USER_NOT_FOUND, "가입되지 않은 이메일입니다.");
        }
        
        // 인증번호 발송 (도메인 체크 X)
        emailVerificationService.sendCode(email);
    }

    // 계정 찾기용 인증번호 검증 (검증 완료 처리)
    @Transactional
    public void verifyCodeForAccountRecovery(String email, String code) {
        emailVerificationService.verifyCode(email, code);
    }

    @Transactional(readOnly = true)
    public boolean isVerified(String email) {
        return emailVerificationService.isVerified(email);
    }

    @Transactional
    public void clearVerification(String email) {
        emailVerificationService.clearVerification(email);
    }




    // 아이디 찾기 - 인증 후 아이디 반환
    @Transactional
    public String findUsernameByEmail(String email, String code) {
        verifyCodeForAccountRecovery(email, code);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return user.getUsername();
    }

    // 비밀번호 찾기 - 인증번호 발송
    @Transactional
    public void sendVerificationCodeForPasswordReset(String username, String email) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        if (!user.getEmail().equals(email)) {
             throw new CustomException(ErrorCode.BAD_REQUEST, "아이디와 이메일 정보가 일치하지 않습니다.");
        }
        
        sendVerificationCodeForAccountRecovery(email);
    }

    // 비밀번호 찾기 - 인증번호 검증 및 리셋 토큰 발급
    @Transactional
    public String verifyCodeForPasswordReset(String email, String code) {
        verifyCodeForAccountRecovery(email, code);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        return jwtTokenProvider.createPasswordResetToken(user.getId());
    }

    // 비밀번호 재설정 (비밀번호 찾기)
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        if (!jwtTokenProvider.validateToken(resetToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "유효하지 않거나 만료된 토큰입니다.");
        }
        
        Claims claims = jwtTokenProvider.parseClaims(resetToken);
        String type = claims.get("type", String.class);
        if (!"password_reset".equals(type)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "비밀번호 재설정용 토큰이 아닙니다.");
        }
        
        Long userId = Long.valueOf(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이전 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    // 아이디 중복 체크
    @Transactional(readOnly = true)
    public boolean checkUsernameAvailability(String username) {
        return !userRepository.existsByUsername(username);
    }

    // 학생 회원 가입
    @Transactional
    public Long signupStudent(StudentSignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 아이디입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .role(Role.ROLE_STUDENT)
                .socialType(SocialType.LOCAL)
                .email(request.getEmail())
                .build();

        userRepository.save(user);

        createStudentProfile(user, request.getUniversityId(), request.getNickname(), request.getCollegeId(), request.getDepartmentId());

        return user.getId();
    }

    // 점주 회원 가입
    @Transactional
    public Long signupOwner(OwnerSignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 아이디입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .role(Role.ROLE_OWNER)
                .socialType(SocialType.LOCAL)
                .email(request.getEmail())
                .build();

        userRepository.save(user);

        createOwnerProfile(user, request.getName());

        return user.getId();
    }

    @Transactional
    public Long signupCouncil(CouncilSignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 아이디입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_COUNCIL)
                .socialType(SocialType.LOCAL)
                .build();

        userRepository.save(user);

        createCouncilProfile(user, request.getUniversityId());

        return user.getId();
    }

    @Transactional
    public AuthTokens login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
            User user = principal.getUser();

            log.info("[Login] Success. userId={}", user.getId());
            return generateTokenResponse(user);
        } catch (BadCredentialsException e) {
            log.warn("[Login] Failed. Invalid credentials for username: {}", request.getUsername());
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }
    }

    @Transactional
    public AuthTokens refresh(String refreshToken) {
        // 토큰 유효성 검사
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("[RefreshToken] Invalid token provided.");
            throw new CustomException(ErrorCode.INVALID_TOKEN, "유효하지 않은 리프레시 토큰입니다.");
        }

        // 토큰에서 UserId 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // DB 비교
        String storedToken = refreshTokenService.getByUserId(userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            log.warn("[RefreshToken] Token mismatch or not found in RefreshToken Table. userId={}", userId);
            refreshTokenService.delete(userId);
            throw new CustomException(ErrorCode.INVALID_TOKEN, "리프레시 토큰이 만료되었거나 일치하지 않습니다.");
        }

        // 유저 조회 (유일한 DB 조회)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 토큰 재발급 (Rotation)
        log.info("[RefreshToken] Success. Tokens rotated for userId={}", userId);
        return generateTokenResponse(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            Long userId = jwtTokenProvider.getUserId(refreshToken);
            refreshTokenService.delete(userId);
        }
    }

    @Transactional
    public AuthTokens completeSocialSignup(Long userId, CompleteSocialSignupRequest request) {
        log.info("completeSocialSignup userId: {}, request: {}", userId, request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.ROLE_GUEST) {
            log.error("completeSocialSignup already complete. userId: {}, role: {}", userId, user.getRole());
            throw new CustomException(ErrorCode.STATE_CONFLICT, "이미 가입이 완료된 회원입니다.");
        }

        // 권한 업데이트
        user.changeRole(request.getRole());

        if (request.getRole() == Role.ROLE_STUDENT) {
            // 학생 로직
            user.updateEmail(request.getEmail());
            createStudentProfile(user, request.getUniversityId(), request.getNickname(), request.getCollegeId(), request.getDepartmentId());
        } else if (request.getRole() == Role.ROLE_OWNER) {
            // 점주 로직
            user.updateEmail(request.getEmail()); // 소셜 가입 시 이메일 업데이트 필요
            createOwnerProfile(user, request.getName());

        } else if (request.getRole() == Role.ROLE_COUNCIL) {
            // 학생회 로직
            createCouncilProfile(user, request.getUniversityId());
        }

        // 변경된 Role로 토큰 재발급
        return generateTokenResponse(user);
    }

    @Transactional
    public void withdraw(User user, WithdrawRequest request) {

        if (request.getReasons().contains(WithdrawalReason.OTHER)) {
            if (request.getDetailReason() == null || request.getDetailReason().trim().isEmpty()) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "기타 사유 선택 시 상세 내용은 필수입니다.");
            }
        }

        // 피드백 저장
        WithdrawalFeedback feedback = WithdrawalFeedback.builder()
                .user(user)
                .reasons(request.getReasons())
                .detailReason(request.getDetailReason())
                .build();
        withdrawalFeedbackRepository.save(feedback);

        // 유저 소프트 딜리트
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        currentUser.withdraw();

        // 리프레시 토큰 삭제
        refreshTokenService.delete(user.getId());
    }
    
    private void createStudentProfile(User user, Long universityId, String nickname, Long collegeId, Long departmentId) {

        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 대학을 찾을 수 없습니다."));

        if (collegeId != null) {
            Organization college = organizationRepository.findById(collegeId)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 단과대학을 찾을 수 없습니다."));
            userOrganizationRepository.save(new UserOrganization(user, college));
        }

        if (departmentId != null) {
            Organization department = organizationRepository.findById(departmentId)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 학과를 찾을 수 없습니다."));
            
            // 학과 선택 시 단과대학 정보가 없으면 에러
            if (collegeId == null) {
                 throw new CustomException(ErrorCode.BAD_REQUEST, "학과를 선택하려면 단과대학을 먼저 선택해야 합니다.");
            }
            
            // 선택한 학과가 선택한 단과대학 소속인지 확인
            if (department.getParent() == null || !department.getParent().getId().equals(collegeId)) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "선택한 학과가 해당 단과대학에 속하지 않습니다.");
            }

            userOrganizationRepository.save(new UserOrganization(user, department));
        }

        StudentProfile profile = StudentProfile.builder()
                .user(user)
                .nickname(nickname)
                .university(university)
                .build();
        studentProfileRepository.save(profile);



    }

    private void createOwnerProfile(User user, String name) {
        OwnerProfile profile = OwnerProfile.builder()
                .user(user)
                .name(name)
                .build();
        ownerProfileRepository.save(profile);
    }

    private void createCouncilProfile(User user, Long universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 대학을 찾을 수 없습니다."));

        CouncilProfile profile = CouncilProfile.builder()
                .user(user)
                .university(university)
                .build();

        councilProfileRepository.save(profile);
    }

    private AuthTokens generateTokenResponse(User user) {

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getUsername(), user.getRole().name());

        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getId(), user.getUsername(), user.getRole().name());

        refreshTokenService.save(user.getId(), refreshToken);

        return new AuthTokens(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpiresIn());
    }
}
