package com.looky.domain.user.dto;

import com.looky.domain.user.entity.Gender;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerSignupRequest {
    @Pattern(regexp = "^[a-zA-Z0-9]{6,16}$", message = "아이디는 영문, 숫자 포함 6자 이상 16자 이하여야 합니다.")
    private String username; // 아이디 (이메일X)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,20}$", message = "비밀번호는 영어, 숫자, 특수문자를 포함한 8자 이상 20자 이하여야 합니다.")
    private String password;
    private String email;
    private Gender gender;
    private LocalDate birthDate;

    private String name;
}
