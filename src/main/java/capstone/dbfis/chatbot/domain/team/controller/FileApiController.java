package capstone.dbfis.chatbot.domain.team.controller;

import capstone.dbfis.chatbot.domain.team.service.S3Service;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.HashMap;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileApiController {

    private final S3Service s3Service;
    private final TokenProvider tokenProvider;

    // 파일 업로드
    @PostMapping("/upload")
    @Operation(summary = "파일 업로드",
            description = "JWT 토큰을 통해 사용자의 팀 접근 권한을 검증하고, 해당 팀의 파일을 S3에 업로드합니다. 업로드된 파일은 팀별로 폴더에 저장되며, 파일의 메타데이터에는 업로더의 정보가 포함됩니다. 업로드가 성공하면 파일 URL이 반환됩니다.")
    public ResponseEntity<String> uploadFile(@RequestHeader("Authorization") String token,
                                             @RequestParam("file") MultipartFile file,
                                             @RequestParam("teamId") Long teamId) {
        String uploaderId = tokenProvider.getMemberId(token);
        try {
            String fileUrl = s3Service.uploadFile(file, teamId, uploaderId);
            return ResponseEntity.ok("파일 업로드 성공: " + fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패: " + e.getMessage());
        }
    }

    // 팀 파일 목록 조회
    @GetMapping("/list")
    @Operation(summary = "팀 파일 목록 조회",
            description = "JWT 토큰을 통해 사용자의 팀 접근 권한을 검증하고, 해당 팀의 파일 목록을 조회합니다. 파일 목록에는 파일 이름, 크기, 마지막 수정 날짜, 게시자 ID 및 게시자 이름 등이 포함됩니다. 사용자는 팀에 속해 있어야만 파일 목록을 조회할 수 있습니다.")
    public ResponseEntity<List<Map<String, Object>>> listFiles(@RequestHeader("Authorization") String token, @RequestParam Long teamId) {
        String viewerId = tokenProvider.getMemberId(token);
        List<Map<String, Object>> files = s3Service.listFiles(teamId, viewerId);
        return ResponseEntity.ok(files);
    }

    // 파일 다운로드
    @GetMapping("/download")
    @Operation(summary = "팀 파일 다운로드",
            description = "JWT 토큰을 통해 사용자의 팀 접근 권한을 검증하고, 해당 팀의 파일을 다운로드합니다. 다운로드 시 파일 이름을 URL 인코딩하여 전송하며, 파일은 'Content-Disposition' 헤더를 통해 자동으로 다운로드됩니다.")
    public ResponseEntity<Resource> downloadFile(@RequestHeader("Authorization") String token,
                                                 @RequestParam String fileName,
                                                 @RequestParam Long teamId) {
        String downloaderId = tokenProvider.getMemberId(token);
        // s3에서 파일 가져오기
        Resource resource = s3Service.downloadFile(teamId, fileName, downloaderId);
        // 파일의 content type 가져오기
        String contentType = s3Service.getFileContentType(fileName);
        try {
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("파일명 URL 인코딩 실패", e);
        }
    }

    // 파일 삭제
    @DeleteMapping("/delete")
    @Operation(summary = "파일 삭제",
            description = "JWT 토큰을 통해 사용자의 팀 접근 권한을 검증하고, 해당 팀의 파일을 삭제합니다. 파일 삭제는 파일의 업로더 ID 또는 팀 리더 권한을 가진 사용자만 할 수 있습니다. 삭제 성공 시 파일이 시스템에서 제거됩니다.")
    public ResponseEntity<Map<String, String>> deleteFile(@RequestHeader("Authorization") String token,
                                                          @RequestParam Long teamId,
                                                          @RequestParam String fileName) {
        String requesterId = tokenProvider.getMemberId(token);
        Map<String, String> response = new HashMap<>();
        try {
            // 파일 이름 URL 디코딩
            String decodedFileName = URLDecoder.decode(fileName, "UTF-8");

            // s3에서 파일 삭제
            s3Service.deleteFile(teamId, decodedFileName, requesterId);
            response.put("message", "파일 삭제 성공");
            return ResponseEntity.ok(response);
        } catch (UnsupportedEncodingException e) {
            response.put("message", "파일 이름 디코딩 실패");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IllegalStateException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            response.put("message", "파일 삭제 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}