package capstone.dbfis.chatbot.domain.filesharing.controller;

import capstone.dbfis.chatbot.domain.filesharing.service.FileSharingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public-reports")
@Tag(name = "PublicReport API", description = "Public Report 관리 API (flask tool 내부 호출용)")
public class PublicReportController {

    private final FileSharingService fileSharingService;

    @Operation(summary = "트렌드 리포트 업로드", description = "새로운 트렌드 리포트를 업로드합니다.")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadReport(
            @RequestParam("file") @NotNull MultipartFile file) {

        // S3에 리포트 업로드하고 key 반환
        String key = fileSharingService.uploadPublicReport(file);

        // presigned URL 생성
        String url = fileSharingService.generatePublicReportPresignedUrl(key);

        // 201 Created 반환
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("url", url));
    }
}
