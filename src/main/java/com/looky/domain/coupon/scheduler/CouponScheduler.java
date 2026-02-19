package com.looky.domain.coupon.scheduler;

import com.looky.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponScheduler {

    private final CouponService couponService;

    // 1분마다 실행
    @Scheduled(cron = "0 * * * * *")
    public void scheduleCouponReset() {
        int count = couponService.resetExpiredCoupons();

        if (count > 0) {
            log.info("[Scheduler] 만료된 쿠폰 {}개 초기화 완료", count);
        }
    }

    // 10분마다 만료 체크 실행
    @Scheduled(fixedDelay = 600000)
    public void scheduleCouponExpirationCheck() {
        couponService.expireOutdatedCoupons();
        log.info("[Scheduler] 쿠폰 만료 체크 실행 완료");
    }
}
