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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

            // 1단계: 엑셀 전체 파싱 (유효한 행만)
            record StoreRow(Long universityId, String name, String branch, String roadAddress, String jibunAddress, Double latitude, Double longitude) {}

            List<StoreRow> storeRows = new ArrayList<>();
            Set<String> allNames = new HashSet<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name = getCellValueAsString(row.getCell(1));
                if (name.isBlank()) continue;

                storeRows.add(new StoreRow(
                        getCellValueAsLong(row.getCell(0)),
                        name,
                        getCellValueAsString(row.getCell(2)),
                        getCellValueAsString(row.getCell(3)),
                        getCellValueAsString(row.getCell(4)),
                        getCellValueAsDouble(row.getCell(5)),
                        getCellValueAsDouble(row.getCell(6))
                ));
                allNames.add(name);
            }

            // 2단계: DB에서 관련 가게 한 번에 조회 → Map<"name||roadAddress", Store>로 인덱싱
            Map<String, Store> storeMap = storeRepository.findAllByNameIn(allNames)
                    .stream()
                    .collect(Collectors.toMap(
                            s -> s.getName() + "||" + s.getRoadAddress(),
                            s -> s,
                            (a, b) -> a  // DB에 중복이 있어도 첫 번째 유지
                    ));

            // 3단계: 행별 처리 - Map 기준으로 신규/업데이트 분기
            List<Store> newStores = new ArrayList<>();

            for (StoreRow storeRow : storeRows) {
                String key = storeRow.name() + "||" + storeRow.roadAddress();
                Store store = storeMap.get(key);

                if (store != null) {
                    // 기존 가게 → 엑셀 데이터로 덮어쓰기
                    store.updateStore(
                            storeRow.name(),
                            storeRow.branch(),
                            storeRow.roadAddress(),
                            storeRow.jibunAddress(),
                            storeRow.latitude() != null ? storeRow.latitude() : store.getLatitude(),
                            storeRow.longitude() != null ? storeRow.longitude() : store.getLongitude(),
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
                    // 신규 가게 생성 후 Map에 즉시 등록 → 배치 내 중복 방지
                    store = Store.builder()
                            .name(storeRow.name())
                            .branch(storeRow.branch())
                            .roadAddress(storeRow.roadAddress())
                            .jibunAddress(storeRow.jibunAddress())
                            .latitude(storeRow.latitude())
                            .longitude(storeRow.longitude())
                            .storeStatus(StoreStatus.UNCLAIMED)
                            .user(null)
                            .build();
                    storeMap.put(key, store);
                    newStores.add(store);
                }

                // 학교 연결 (기존 데이터에 계속 추가)
                if (storeRow.universityId() != null) {
                    universityRepository.findById(storeRow.universityId()).ifPresent(store::addUniversity);
                }
            }

            // 신규 가게만 INSERT (기존 가게는 JPA dirty checking으로 자동 UPDATE)
            storeRepository.saveAll(newStores);

            // 비동기 지오코딩 트리거 (신규 가게 중 좌표 없는 것)
            newStores.stream()
                    .filter(s -> s.getLatitude() == null || s.getLongitude() == null)
                    .forEach(s -> geocodingService.updateLocation(s.getId(), s.getRoadAddress()));

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
