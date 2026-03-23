package com.looky.domain.user.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.response.PageResponse;
import com.looky.domain.user.dto.AdminUserResponse;
import com.looky.domain.user.dto.UserRoleUpdateRequest;
import com.looky.domain.user.entity.User;
import com.looky.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getAllUsersForAdmin(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        Page<AdminUserResponse> userResponses = users.map(AdminUserResponse::from);
        return PageResponse.from(userResponses);
    }

    @Transactional
    public void updateUserRoleForAdmin(Long userId, UserRoleUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.changeRole(request.getRole());
    }
}
