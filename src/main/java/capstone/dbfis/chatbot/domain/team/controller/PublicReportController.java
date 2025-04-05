package capstone.dbfis.chatbot.domain.team.controller;

import capstone.dbfis.chatbot.domain.team.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public-reports")
@Tag(name = "PublicReport API", description = "Public Report 관리 API (flask tool 내부 호출용)")
public class PublicReportController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    @Operation(summary = "트렌드 리포트 업로드", description = "새로운 트렌드 리포트를 업로드합니다.")
    public ResponseEntity<Map<String, String>> uploadReport(@RequestParam("file") MultipartFile file) {
        String key = s3Service.uploadPublicReport(file);
        String url = s3Service.generatePresignedUrl(key);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
