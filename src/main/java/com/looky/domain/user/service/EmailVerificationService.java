package com.looky.domain.user.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.organization.entity.University;
import com.looky.domain.organization.repository.UniversityRepository;
import com.looky.domain.user.entity.EmailVerification;
import com.looky.domain.user.repository.EmailVerificationRepository;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UniversityRepository universityRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromAddress;

    @Value("${app.email.verification.ttl-minutes:5}")
    private long ttlMinutes;

    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    // 이메일 인증 번호 발송 (회원가입 용)
    @Transactional
    public void sendCode(String email, Long universityId) {

        // universityId가 있으면 해당 대학 도메인과 일치하는지 검증
        if (universityId != null) {
            validateEmailDomain(email, universityId);
        }

        // 이미 가입된 이메일인지 확인 
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 가입된 이메일입니다.");
        }

        sendCodeInternal(email);
    }

    // 이메일 인증 번호 발송 (계정 찾기 등 공통 용도 - 도메인 체크 없음)
    @Transactional
    public void sendCode(String email) {
        sendCodeInternal(email);
    }

    // 공통 인증 번호 발송
    private void sendCodeInternal(String email) {
        String code = generateCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(ttlMinutes);

        EmailVerification ev = emailVerificationRepository.findByEmail(email)
                .orElseGet(() -> new EmailVerification(email, code, expiresAt));
        
        ev.updateCode(code, expiresAt);
        emailVerificationRepository.save(ev);

        sendMail(email, code);
    }

    // 이메일 인증 번호 검증
    @Transactional
    public void verifyCode(String email, String code) {
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

    private void validateEmailDomain(String email, Long universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 대학을 찾을 수 없습니다."));
        
        String domain = university.getEmailDomain();
        if (domain == null || domain.isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "해당 대학에 이메일 도메인이 존재하지 않습니다.");
        }

        String normalizedEmail = email.toLowerCase(Locale.ROOT);
        String normalizedDomain = domain.toLowerCase(Locale.ROOT);

        if (!normalizedEmail.endsWith("@" + normalizedDomain)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, university.getName() + " 이메일 계정만 인증 가능합니다.");
        }
    }

    private void sendMail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(fromAddress);
        message.setSubject("[Looky] 이메일 인증번호 안내");
        message.setText(
                "안녕하세요.\n" +
                        "Looky 앱 이메일 인증을 요청하셨습니다.\n\n" +
                        "인증번호: " + code + "\n\n" +
                        "해당 코드는 " + ttlMinutes + "분 후 만료됩니다.\n"
        );
        mailSender.send(message);
    }

    private String generateCode() {
        int bound = 1;
        for (int i = 0; i < CODE_LENGTH; i++) bound *= 10;
        int number = random.nextInt(bound);
        return String.format("%0" + CODE_LENGTH + "d", number);
    }
}
