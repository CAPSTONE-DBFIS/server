package capstone.dbfis.chatbot.domain.team.controller;

import capstone.dbfis.chatbot.domain.team.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public-reports")
public class PublicReportController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadReport(@RequestParam("file") MultipartFile file) {
        String key = s3Service.uploadPublicReport(file);
        String url = s3Service.generatePresignedUrl(key);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
