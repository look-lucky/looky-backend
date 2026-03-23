package com.looky.domain.user.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.user.dto.ChangePasswordRequest;
import com.looky.domain.user.dto.ChangeUsernameRequest;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changeUsername(Long userId, ChangeUsernameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (userRepository.existsByUsername(request.getNewUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 아이디입니다.");
        }

        user.updateUsername(request.getNewUsername());
    }

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
}
