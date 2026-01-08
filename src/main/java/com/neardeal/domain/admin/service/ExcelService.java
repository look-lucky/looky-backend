package com.neardeal.domain.admin.service;

import com.neardeal.domain.organization.entity.Organization;
import com.neardeal.domain.store.entity.Store;
import com.neardeal.domain.store.entity.StoreOrganization;
import com.neardeal.domain.store.repository.StoreOrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final StoreOrganizationRepository storeOrganizationRepository;

    public byte[] downloadXlsx() throws IOException {
        List<StoreOrganization> storeOrganizations = storeOrganizationRepository.findAllWithStoreAndOrganization();

        // 엑셀 Workbook 객체 생성
        Workbook workbook = new XSSFWorkbook();

        // 시트 추가
        Sheet sheet = workbook.createSheet("AffiliateBenefits");

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "Store Name", "University Name", "Organization Name", "Org Category", "Benefit Content" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 데이터 채우기
        int rowNum = 1;
        for (StoreOrganization so : storeOrganizations) {
            Row row = sheet.createRow(rowNum++);

            Store store = so.getStore();
            Organization org = so.getOrganization();

            row.createCell(0).setCellValue(so.getId()); // StoreOrganization ID
            row.createCell(1).setCellValue(store.getName());
            row.createCell(2).setCellValue(org.getUniversity().getName());
            row.createCell(3).setCellValue(org.getName());
            row.createCell(4).setCellValue(org.getCategory().toString());
            row.createCell(5).setCellValue(so.getBenefit());
        }

        // 컬럼 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 출력 스트림화
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    @Transactional
    public void uploadXlsx(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트

        // 1번째 행부터 데이터 읽기 (0번째는 헤더)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null)
                continue;

            Cell idCell = row.getCell(0);
            Cell benefitCell = row.getCell(5);

            if (idCell == null)
                continue;

            // ID 읽기 (Numeric으로 읽힘)
            Long id = (long) idCell.getNumericCellValue();

            // Benefit 읽기
            String benefit = "";
            if (benefitCell != null) {
                benefit = getCellValueAsString(benefitCell);
            }

            // DB 업데이트
            Long finalId = id;
            String finalBenefit = benefit;
            storeOrganizationRepository.findById(finalId).ifPresent(so -> {so.updateBenefit(finalBenefit);
            });
        }

        workbook.close();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
