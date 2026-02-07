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
import com.looky.domain.user.repository.EmailVerificationRepository;
import com.looky.domain.user.repository.OwnerProfileRepository;
import com.looky.domain.user.repository.StudentProfileRepository;
import com.looky.domain.user.repository.UserRepository;
import com.looky.domain.user.repository.WithdrawalFeedbackRepository;
import com.looky.security.details.PrincipalDetails;
import com.looky.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.security.SecureRandom;
import java.util.Locale;

import java.time.LocalDateTime;


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

    private static final int CODE_LENGTH = 6;
    private static final String ALLOWED_DOMAIN = "jbnu.ac.kr";
    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    @Value("${app.email.from}")
    private String fromAddress;

    @Value("${app.email.verification.ttl-minutes:5}")
    private long ttlMinutes;

    private final SecureRandom random = new SecureRandom();

    // 학생 이메일 인증
    @Transactional
    public void sendVerificationCode(String email) {
        validateEmailDomain(email);
        if (emailVerificationRepository.existsByEmailAndVerifiedTrue(email)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미 인증된 이메일입니다.");
        }
        String code = generateCode();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(ttlMinutes);

        EmailVerification ev = emailVerificationRepository.findByEmail(email)
                .orElseGet(() -> new EmailVerification(email, code, expiresAt));
        ev.updateCode(code, expiresAt);
        emailVerificationRepository.save(ev);
        sendMail(email, code);
    }

    @Transactional
    public void verifyCode(String email, String code) {
        validateEmailDomain(email);

        LocalDateTime now = LocalDateTime.now();

        EmailVerification ev = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_CODE_EXPIRED));

        if (ev.isVerified()) {
            return;
        }

        if (ev.isCodeExpired(now)) {
            emailVerificationRepository.delete(ev);
            throw new CustomException(ErrorCode.EMAIL_CODE_EXPIRED);
        }

        if (!ev.getCode().equals(code)) {
            throw new CustomException(ErrorCode.EMAIL_CODE_MISMATCH);
        }

        ev.markVerifiedPermanent();
        emailVerificationRepository.save(ev);
    }

    @Transactional(readOnly = true)
    public boolean isVerified(String email) {
        return emailVerificationRepository.existsByEmailAndVerifiedTrue(email);
    }

    @Transactional
    public void clearVerification(String email) {
        emailVerificationRepository.findByEmail(email)
                .ifPresent(EmailVerification::clearVerified);
    }

    private void sendMail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(fromAddress);
        message.setSubject("[Looky] 이메일 인증번호 안내");
        message.setText(
            "안녕하세요.\n" +
            "Looky 앱 학생 이메일 인증을 요청하셨습니다.\n\n" +
            "인증번호: " + code + "\n\n" +
            "해당 코드는 5분 후 만료됩니다.\n"
        );
        mailSender.send(message);
    }

    private String generateCode() {
        int bound = 1;
        for (int i = 0; i < CODE_LENGTH; i++) bound *= 10;
        int number = random.nextInt(bound);
        return String.format("%0" + CODE_LENGTH + "d", number);
    }

    private void validateEmailDomain(String email) {
        if (email == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이메일은 필수 입력값입니다.");
        }
        String normalized = email.toLowerCase(Locale.ROOT);
        if (!normalized.endsWith("@" + ALLOWED_DOMAIN)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "전북대학교 이메일 계정만 인증 가능합니다.");
        }
    }
    



    // 아이디 중복 체크
    @Transactional(readOnly = true)
    public boolean checkUsernameAvailability(String username) {
        return !userRepository.existsByUsername(username);
    }

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
                .build();

        userRepository.save(user);

        createStudentProfile(user, request.getUniversityId(), request.getNickname(), request.getCollegeId(), request.getDepartmentId());

        return user.getId();
    }

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
                .name(request.getName())
                .role(Role.ROLE_OWNER)
                .socialType(SocialType.LOCAL)
                .build();

        userRepository.save(user);

        createOwnerProfile(user, request.getName(), request.getEmail(), request.getPhone());

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

        // Redis 비교
        String storedToken = refreshTokenService.getByUserId(userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            log.warn("[RefreshToken] Token mismatch or not found in Redis. userId={}", userId);
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.ROLE_GUEST) {
            throw new CustomException(ErrorCode.STATE_CONFLICT, "이미 가입이 완료된 회원입니다.");
        }

        // 권한 업데이트
        user.changeRole(request.getRole());

        if (request.getRole() == Role.ROLE_STUDENT) {
            // 학생 로직
            createStudentProfile(user, request.getUniversityId(), request.getNickname(), request.getCollegeId(), request.getDepartmentId());
        } else if (request.getRole() == Role.ROLE_OWNER) {
            // 점주 로직
            createOwnerProfile(user, request.getName(), request.getEmail(), request.getPhone());

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
            userOrganizationRepository.save(new UserOrganization(user, department));
        }

        StudentProfile profile = StudentProfile.builder()
                .user(user)
                .nickname(nickname)
                .university(university)
                .build();
        studentProfileRepository.save(profile);



    }

    private void createOwnerProfile(User user, String name, String email, String phone) {
        OwnerProfile profile = OwnerProfile.builder()
                .user(user)
                .name(name)
                .email(email)
                .phone(phone)
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
