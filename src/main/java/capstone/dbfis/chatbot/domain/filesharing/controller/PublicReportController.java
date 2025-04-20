package capstone.dbfis.chatbot.domain.filesharing.controller;

import capstone.dbfis.chatbot.domain.filesharing.service.FileSharingService;
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

    private final FileSharingService fileSharingService;

    @PostMapping("/upload")
    @Operation(summary = "트렌드 리포트 업로드", description = "새로운 트렌드 리포트를 업로드합니다.")
    public ResponseEntity<Map<String, String>> uploadReport(@RequestParam("file") MultipartFile file) {
        String key = fileSharingService.uploadPublicReport(file);
        String url = fileSharingService.generatePublicReportPresignedUrl(key);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
