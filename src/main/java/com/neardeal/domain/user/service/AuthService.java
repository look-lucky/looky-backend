package com.neardeal.domain.user.service;

import com.neardeal.common.exception.CustomException;
import com.neardeal.common.exception.ErrorCode;
import com.neardeal.domain.organization.entity.University;
import com.neardeal.domain.organization.repository.UniversityRepository;
import com.neardeal.domain.user.dto.*;
import com.neardeal.domain.organization.entity.Organization;
import com.neardeal.domain.organization.entity.UserOrganization;
import com.neardeal.domain.organization.repository.OrganizationRepository;
import com.neardeal.domain.organization.repository.UserOrganizationRepository;
import com.neardeal.domain.store.entity.Store;
import com.neardeal.domain.store.entity.StoreOrganization;
import com.neardeal.domain.store.repository.StoreOrganizationRepository;
import com.neardeal.domain.store.repository.StoreRepository;
import com.neardeal.domain.user.entity.*;
import com.neardeal.domain.user.repository.CouncilProfileRepository;
import com.neardeal.domain.user.repository.StudentProfileRepository;
import com.neardeal.domain.user.repository.OwnerProfileRepository;
import com.neardeal.domain.user.repository.UserRepository;
import com.neardeal.security.details.PrincipalDetails;
import com.neardeal.security.jwt.JwtTokenProvider;
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
    private final StoreRepository storeRepository;
    private final StoreOrganizationRepository storeOrganizationRepository;
    private final OrganizationRepository organizationRepository;
    private final UserOrganizationRepository userOrganizationRepository;



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
                .role(Role.ROLE_CUSTOMER)
                .socialType(SocialType.LOCAL)
                .build();

        userRepository.save(user);

        createCustomerProfile(user, request.getUniversityId(), request.getNickname());

        if (request.getCollegeId() != null) {
            Organization college = organizationRepository.findById(request.getCollegeId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 단과대학을 찾을 수 없습니다."));
            userOrganizationRepository.save(new UserOrganization(user, college));
        }
        if (request.getDepartmentId() != null) {
            Organization department = organizationRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 학과를 찾을 수 없습니다."));
            userOrganizationRepository.save(new UserOrganization(user, department));
        }

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
                .role(Role.ROLE_OWNER)
                .socialType(SocialType.LOCAL)
                .build();

        userRepository.save(user);

        createOwnerProfile(user, request);

        // Stores
        if (request.getStoreList() != null) {
            for (OwnerSignupRequest.StoreCreateRequest storeReq : request.getStoreList()) {
                Store store = Store.builder()
                        .user(user)
                        .name(storeReq.getName())
                        .address(storeReq.getAddress())
                        .businessRegistrationNumber(storeReq.getBusinessRegistrationNumber())
                        .build();
                storeRepository.save(store);

                storeRepository.save(store);

                if (storeReq.getPartnerOrganizationIds() != null) {
                    for (Long orgId : storeReq.getPartnerOrganizationIds()) {
                        Organization org = organizationRepository.findById(orgId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "제휴 단체를 찾을 수 없습니다."));
                        storeOrganizationRepository.save(new StoreOrganization(store, org, null));
                    }
                }
            }
        }

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
                .role(Role.ROLE_CUSTOMER)
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

        if (request.getRole() == Role.ROLE_CUSTOMER) {
            // 학생 로직
            createCustomerProfile(user, request.getUniversityId(), request.getNickname());
            
            if (request.getCollegeId() != null) {
                Organization college = organizationRepository.findById(request.getCollegeId())
                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 단과대학을 찾을 수 없습니다."));
                userOrganizationRepository.save(new UserOrganization(user, college));
            }
            if (request.getDepartmentId() != null) {
                Organization department = organizationRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 학과를 찾을 수 없습니다."));
                userOrganizationRepository.save(new UserOrganization(user, department));
            }
            
        } else if (request.getRole() == Role.ROLE_OWNER) {
            // 점주 로직
            OwnerProfile profile = OwnerProfile.builder()
                    .user(user)
                    .name(request.getName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .build();
            ownerProfileRepository.save(profile);

            // 상점 임시 등록
            if (request.getStoreList() != null) {
                for (OwnerSignupRequest.StoreCreateRequest storeReq : request.getStoreList()) {
                     Store store = Store.builder()
                             .user(user)
                             .name(storeReq.getName())
                             .address(storeReq.getAddress())
                             .businessRegistrationNumber(storeReq.getBusinessRegistrationNumber())
                             .build();
                     storeRepository.save(store);
                     
                     if (storeReq.getPartnerOrganizationIds() != null) {
                         for (Long orgId : storeReq.getPartnerOrganizationIds()) {
                             Organization org = organizationRepository.findById(orgId)
                                     .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "제휴 단체를 찾을 수 없습니다."));
                             storeOrganizationRepository.save(new StoreOrganization(store, org, null));
                         }
                     }
                }
            }
        } else if (request.getRole() == Role.ROLE_COUNCIL) {
            // 학생회 로직
            createCouncilProfile(user, request.getUniversityId());
        }

        // 변경된 Role로 토큰 재발급
        return generateTokenResponse(user);
    }

    private void createCustomerProfile(User user, Long universityId, String nickname) {
        if (universityId != null) {
            University university = universityRepository.findById(universityId)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 대학을 찾을 수 없습니다."));
            StudentProfile profile = StudentProfile.builder()
                    .user(user)
                    .university(university)
                    .nickname(nickname)
                    .build();
            studentProfileRepository.save(profile);
        } else {
            // 대학생 아닌 고객 가입
            StudentProfile profile = StudentProfile.builder()
                    .user(user)
                    .nickname(nickname)
                    .build();
            studentProfileRepository.save(profile);
        }

    }

    private void createOwnerProfile(User user, OwnerSignupRequest request) {
        OwnerProfile profile = OwnerProfile.builder()
                .user(user)
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
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