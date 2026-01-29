package com.looky.domain.user.dto;

import com.looky.domain.user.entity.WithdrawalReason;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WithdrawRequest {
    
    @NotEmpty(message = "탈퇴 사유는 필수입니다.")
    private List<WithdrawalReason> reasons;
    
    private String detailReason;
}
