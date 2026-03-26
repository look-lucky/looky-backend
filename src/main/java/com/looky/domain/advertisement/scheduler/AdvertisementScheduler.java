package com.looky.domain.advertisement.scheduler;

import com.looky.domain.advertisement.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdvertisementScheduler {

    private final AdvertisementService advertisementService;

    // 매일 자정 광고 상태 동기화
    @Scheduled(cron = "0 0 0 * * *")
    public void syncAdvertisementStatus() {
        log.info("[Scheduler] 광고 상태 동기화 시작");
        advertisementService.activateScheduledAdvertisements();
        advertisementService.endExpiredAdvertisements();
        log.info("[Scheduler] 광고 상태 동기화 완료");
    }
}
