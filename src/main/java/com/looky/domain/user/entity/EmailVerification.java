package com.looky.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "email_verifications",
        uniqueConstraints = @UniqueConstraint(name = "uk_email_verifications_email", columnNames = "email")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean verified;

    @Column
    private LocalDateTime verifiedExpiresAt;

    public EmailVerification(String email, String code, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
        this.verified = false;
        this.verifiedExpiresAt = expiresAt;
    }

    public boolean isCodeExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public boolean isVerifiedValid(LocalDateTime now) {
        return verified && (verifiedExpiresAt == null || verifiedExpiresAt.isAfter(now));
    }

    public void updateCode(String code, LocalDateTime expiresAt) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.verified = false;
        this.verifiedExpiresAt = expiresAt;
    }

    public void markVerified(LocalDateTime verifiedExpiresAt) {
        this.verified = true;
        this.verifiedExpiresAt = verifiedExpiresAt;
    }

    public void markVerifiedPermanent() {
        this.verified = true;
        this.verifiedExpiresAt = null;
    }

    public void clearVerified() {
        this.verified = false;
        this.verifiedExpiresAt = null;
    }
}
