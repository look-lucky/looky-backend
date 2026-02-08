package com.looky.domain.admin.service;

import com.looky.common.exception.CustomException;
import com.looky.common.exception.ErrorCode;
import com.looky.domain.organization.entity.Organization;
import com.looky.domain.organization.entity.UserOrganization;
import com.looky.domain.organization.entity.University;
import com.looky.domain.organization.repository.OrganizationRepository;
import com.looky.domain.organization.repository.UniversityRepository;
import com.looky.domain.organization.repository.UserOrganizationRepository;
import com.looky.domain.partnership.dto.CreatePartnershipRequest;
import com.looky.domain.partnership.dto.PartnershipResponse;
import com.looky.domain.partnership.dto.UpdatePartnershipRequest;
import com.looky.domain.partnership.entity.Partnership;
import com.looky.domain.partnership.repository.PartnershipRepository;
import com.looky.domain.store.entity.Store;
import com.looky.domain.store.entity.StoreUniversity;
import com.looky.domain.store.repository.StoreRepository;
import com.looky.domain.store.repository.StoreUniversityRepository;
import com.looky.domain.user.entity.Role;
import com.looky.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminPartnershipService {

    private final StoreRepository storeRepository;
    private final OrganizationRepository organizationRepository;
    private final PartnershipRepository partnershipRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final StoreUniversityRepository storeUniversityRepository;
    private final UniversityRepository universityRepository;

    private static final String[] HEADERS = {
            "universityId", "storeId", "storeName", "branch", "roadAddress", "benefitDetail", "startDate", "endDate"
    };

    // 제휴 등록 (Admin)
    @Transactional
    public Long createPartnership(Long universityId, Long organizationId, CreatePartnershipRequest request) {

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "가게를 찾을 수 없습니다."));

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "조직을 찾을 수 없습니다."));

        // 조직이 해당 대학 소속인지 확인
        if (!Objects.equals(organization.getUniversity().getId(), universityId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "해당 대학의 조직이 아닙니다.");
        }

        if (partnershipRepository.existsByStoreIdAndOrganizationId(request.getStoreId(), organizationId)) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 등록된 제휴입니다.");
        }

        Partnership partnership = Partnership
                .builder()
                .store(store)
                .organization(organization)
                .benefit(request.getBenefit())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .build();

        partnershipRepository.save(partnership);

        return partnership.getId();
    }

    // 제휴 수정 (Admin)
    @Transactional
    public void updatePartnershipBenefit(Long partnershipId, UpdatePartnershipRequest request) {

        Partnership partnership = partnershipRepository
                .findById(partnershipId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "제휴 정보를 찾을 수 없습니다."));

        partnership.updateBenefit(request.getBenefit(), request.getStartsAt(), request.getEndsAt());
    }

    // 제휴 삭제 (Admin)
    @Transactional
    public void deletePartnership(Long partnershipId) {

        Partnership partnership = partnershipRepository.findById(partnershipId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "제휴 정보를 찾을 수 없습니다."));

        partnershipRepository.delete(partnership);
    }

    // 대학별 제휴 목록 조회
    public List<PartnershipResponse> getPartnershipsByUniversity(Long universityId) {
        return partnershipRepository.findAllByOrganizationUniversityId(universityId).stream()
                .map(PartnershipResponse::new)
                .collect(Collectors.toList());
    }

    // 조직별 제휴 목록 조회
    public List<PartnershipResponse> getPartnershipsByOrganization(Long universityId, Long organizationId) {
        return partnershipRepository.findAllByOrganizationIdAndOrganizationUniversityId(organizationId, universityId)
                .stream()
                .map(PartnershipResponse::new)
                .collect(Collectors.toList());
    }

    // 엑셀 템플릿 내보내기 (특정 대학 소속 상점 리스트)
    public PartnershipTemplateResult exportPartnershipTemplate(Long universityId) throws IOException {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "대학을 찾을 수 없습니다."));

        String domainPrefix = university.getEmailDomain().split("\\.")[0];
        String filename = domainPrefix + "_partnership_template.xlsx";

        List<StoreUniversity> storeUniversities = storeUniversityRepository.findByUniversityId(universityId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Partnerships");

            // 헤더
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(HEADERS[i]);
            }

            // 본문
            int rowIdx = 1;
            for (StoreUniversity su : storeUniversities) {
                Store store = su.getStore();
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(universityId); // 대학 ID (숨김/보호 로직 추가 가능)
                row.createCell(1).setCellValue(store.getId());
                row.createCell(2).setCellValue(store.getName());
                row.createCell(3).setCellValue(store.getBranch());
                row.createCell(4).setCellValue(store.getRoadAddress());
            }

            workbook.write(out);
            return new PartnershipTemplateResult(out.toByteArray(), filename);
        }
    }

    public record PartnershipTemplateResult(byte[] content, String filename) {
    }

    // 엑셀 업로드 및 처리
    @Transactional
    public void importPartnershipData(MultipartFile file, User user, Long targetOrganizationId) {
        Organization uploaderOrganization = resolveOrganization(user, targetOrganizationId);

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 1. 헤더 검증
            validateHeaders(sheet.getRow(0));

            List<String> errorMessages = new ArrayList<>();
            List<PartnershipData> validDataList = new ArrayList<>();

            // 2. Row별 검증
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                try {
                    PartnershipData data = parseRow(row, i);
                    validateRowData(data, uploaderOrganization, i);
                    validDataList.add(data);
                } catch (IllegalArgumentException e) {
                    errorMessages.add(e.getMessage());
                }
            }

            // 에러가 하나라도 있으면 전체 거부
            if (!errorMessages.isEmpty()) {
                throw new CustomException(ErrorCode.BAD_REQUEST, String.join("\n", errorMessages));
            }

            // 3. DB 반영 (Upsert)
            applyPartnershipUpsert(validDataList, uploaderOrganization);

        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "엑셀 처리 중 오류가 발생했습니다.");
        }
    }

    private Organization resolveOrganization(User user, Long targetOrganizationId) {
        if (user.getRole() == Role.ROLE_ADMIN) {
            if (targetOrganizationId == null) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "관리자는 organizationId 파라미터가 필수입니다.");
            }
            return organizationRepository.findById(targetOrganizationId)
                    .orElseThrow(
                            () -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "해당 Organization을 찾을 수 없습니다."));
        } else if (user.getRole() == Role.ROLE_COUNCIL) {
            return userOrganizationRepository.findByUser(user)
                    .map(UserOrganization::getOrganization)
                    .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "소속된 학생회가 없습니다."));
        } else {
            throw new CustomException(ErrorCode.FORBIDDEN, "권한이 없습니다.");
        }
    }

    private void validateHeaders(Row headerRow) {
        if (headerRow == null)
            throw new CustomException(ErrorCode.BAD_REQUEST, "헤더가 없습니다.");
        for (int i = 0; i < HEADERS.length; i++) {
            String cellValue = getCellValue(headerRow.getCell(i));
            if (!HEADERS[i].equals(cellValue)) {
                throw new CustomException(ErrorCode.BAD_REQUEST,
                        "잘못된 헤더 형식입니다: " + cellValue + " (기대값: " + HEADERS[i] + ")");
            }
        }
    }

    private PartnershipData parseRow(Row row, int lineNum) {
        try {
            Long storeId = Long.parseLong(getCellValue(row.getCell(1)));
            String storeName = getCellValue(row.getCell(2));
            String benefit = getCellValue(row.getCell(5));
            String startDateStr = getCellValue(row.getCell(6));
            String endDateStr = getCellValue(row.getCell(7));

            LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            return new PartnershipData(storeId, storeName, benefit, startDate, endDate);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Line " + lineNum + ": 숫자 형식이 잘못되었습니다.");
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Line " + lineNum + ": 날짜 형식은 yyyy-MM-dd 여야 합니다.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Line " + lineNum + ": 데이터 파싱 오류 - " + e.getMessage());
        }
    }

    private void validateRowData(PartnershipData data, Organization organization, int lineNum) {
        // 1. 상점 존재 여부 확인
        Store store = storeRepository.findById(data.storeId)
                .orElseThrow(() -> new IllegalArgumentException("Line " + lineNum + ": 존재하지 않는 Store ID입니다."));

        // 2. 무결성 검증 (상호명 일치 여부)
        if (!store.getName().trim().equals(data.storeName.trim())) {
            throw new IllegalArgumentException("Line " + lineNum + ": 상호명 불일치 (엑셀: " + data.storeName + ", DB: "
                    + store.getName() + ") - 위조가 의심됩니다.");
        }

        // 3. 관할 구역 검증
        boolean isLinked = storeUniversityRepository.existsByStoreIdAndUniversityId(store.getId(),
                organization.getUniversity().getId());
        if (!isLinked) {
            throw new IllegalArgumentException("Line " + lineNum + ": 해당 상점은 본 학생회의 관할 구역(대학)이 아닙니다.");
        }
    }

    private void applyPartnershipUpsert(List<PartnershipData> validDataList, Organization organization) {
        for (PartnershipData data : validDataList) {
            Partnership partnership = partnershipRepository.findByStoreIdAndOrganizationId(data.storeId,
                    organization.getId());

            if (partnership != null) {
                // 수정
                partnership.updateBenefit(data.benefit, data.startDate, data.endDate);
            } else {
                // 신규 생성
                Store store = storeRepository.getReferenceById(data.storeId);
                partnership = Partnership.builder()
                        .organization(organization)
                        .store(store)
                        .benefit(data.benefit)
                        .startsAt(data.startDate)
                        .endsAt(data.endDate)
                        .build();
                partnershipRepository.save(partnership);
            }
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private record PartnershipData(Long storeId, String storeName, String benefit, LocalDate startDate,
            LocalDate endDate) {
    }
}
