package com.looky.domain.admin.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.common.service.GeocodingService;
import com.looky.domain.organization.repository.UniversityRepository; // Added import
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreStatus;
import com.looky.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminStoreService {

    private final StoreRepository storeRepository;
    private final GeocodingService geocodingService;
    private final UniversityRepository universityRepository;

    @Transactional
    public void uploadStoreData(MultipartFile file) {

        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "파일이 비어있습니다.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // 헤더 검증
            validateHeader(sheet.getRow(0));

            // 헤더 순서
            // 0: UniversityID
            // 1: Name
            // 2: Branch
            // 3: RoadAddress
            // 4: JibunAddress
            // 5: Latitude
            // 6: Longitude

            List<Store> storesToSave = new ArrayList<>();
            List<Long> storeIdsToGeocode = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                Long universityId = getCellValueAsLong(row.getCell(0));
                String name = getCellValueAsString(row.getCell(1));
                String branch = getCellValueAsString(row.getCell(2));
                String roadAddress = getCellValueAsString(row.getCell(3));
                String jibunAddress = getCellValueAsString(row.getCell(4));
                Double latitude = getCellValueAsDouble(row.getCell(5));
                Double longitude = getCellValueAsDouble(row.getCell(6));

                Optional<Store> existingStore = storeRepository.findFirstByNameAndRoadAddress(name, roadAddress);
                Store store;

                // 이미 가게가 존재하는 경우 -> 엑셀 데이터로 덮어쓰기
                if (existingStore.isPresent()) {
                    store = existingStore.get();
                    store.updateStore(
                            // 엑셀 데이터로 덮어쓰기
                            name,
                            branch,
                            roadAddress,
                            jibunAddress,
                            latitude != null ? latitude : store.getLatitude(),
                            longitude != null ? longitude : store.getLongitude(),
                            // 엑셀에 없는 나머지는 기존 데이터 유지
                            store.getStorePhone(),
                            store.getIntroduction(),
                            store.getOperatingHours(),
                            store.getStoreCategories(),
                            store.getStoreMoods(),
                            store.getHolidayDates(),
                            store.getIsSuspended(),
                            store.getRepresentativeName()
                    );
                } else {
                    // 신규 가게 생성
                    store = Store.builder()
                            .name(name)
                            .branch(branch)
                            .roadAddress(roadAddress)
                            .jibunAddress(jibunAddress)
                            .latitude(latitude)
                            .longitude(longitude)
                            .storeStatus(StoreStatus.UNCLAIMED)
                            .user(null) // 주인 없음
                            .build();
                }

                // 학교 연결 (기존 데이터에 계속 추가)
                if (universityId != null) {
                    universityRepository.findById(universityId).ifPresent(store::addUniversity);
                }

                storesToSave.add(store);
            }

            // 일괄 저장 (Insert or Update 쿼리가 여기서 한 번에 발생)
            storeRepository.saveAll(storesToSave);

            // 저장 후 ID가 할당된 Store들 중에서 좌표 없는 건들 찾기
            for (Store savedStore : storesToSave) {
                if (savedStore.getLatitude() == null || savedStore.getLongitude() == null) {
                    storeIdsToGeocode.add(savedStore.getId());
                }
            }

            // 비동기 지오코딩 트리거
            for (Long storeId : storeIdsToGeocode) {
                storeRepository.findById(storeId)
                        .ifPresent(s -> geocodingService.updateLocation(s.getId(), s.getRoadAddress()));
            }

        } catch (IOException e) {
            log.error("Excel upload failed", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "엑셀 업로드 처리 중 오류가 발생했습니다.");
        }
    }

    private Long getCellValueAsLong(Cell cell) {
        if (cell == null)
            return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Long.parseLong(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue()); // 정수로 변환
            default:
                return "";
        }
    }

    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null)
            return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static final String[] EXPECTED_HEADERS = { "universityId", "name", "branch", "roadAddress", "jibunAddress",
            "latitude", "longitude" };

    private void validateHeader(Row headerRow) {
        if (headerRow == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "엑셀 파일에 헤더가 없습니다.");
        }

        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            String cellValue = getCellValueAsString(cell);
            if (!EXPECTED_HEADERS[i].equals(cellValue)) {
                throw new CustomException(ErrorCode.BAD_REQUEST,
                        String.format(
                                "잘못된 헤더 형식입니다. 기대값: '%s', 실제값: '%s' (열: %d). 헤더 순서를 확인해주세요: [universityId, name, branch, roadAddress, jibunAddress, latitude, longitude]",
                                EXPECTED_HEADERS[i], cellValue, i + 1));
            }
        }
    }
}
