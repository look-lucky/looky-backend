package com.neardeal.domain.admin.controller;

import com.neardeal.common.response.CommonResponse;
import com.neardeal.common.response.SwaggerErrorResponse;
import com.neardeal.domain.admin.service.ExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Admin - Excel", description = "관리자 엑셀 관리 API")
@RestController
@RequestMapping("/api/admin/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    @Operation(summary = "[관리자] 제휴 혜택 엑셀 다운로드", description = "상점-조직 제휴 혜택 목록을 엑셀 파일로 다운로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "다운로드 성공"),
            @ApiResponse(responseCode = "500", description = "파일 생성 실패", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExcel() throws IOException {
        byte[] excelBytes = excelService.downloadXlsx();
        String fileName = "partnership_benefits.xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    @Operation(summary = "[관리자] 제휴 혜택 엑셀 업로드", description = "엑셀 파일을 업로드하여 제휴 혜택 내용을 일괄 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드 및 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "파일 처리 실패", content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class)))
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<Void>> uploadExcel(
            @Parameter(description = "업로드할 엑셀 파일 (.xlsx)") @RequestPart("file") MultipartFile file
    ) throws IOException {
        excelService.uploadXlsx(file);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
