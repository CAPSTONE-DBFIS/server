package capstone.dbfis.chatbot.domain.trackingkeyword.controller;

import capstone.dbfis.chatbot.domain.trackingkeyword.dto.*;
import capstone.dbfis.chatbot.domain.trackingkeyword.service.TrackingKeywordService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking-keywords")
@Tag(name = "Tracking Keyword API", description = "추적 키워드 관리 API")
@RequiredArgsConstructor
public class TrackingKeywordController {
    private final TokenProvider tokenProvider;
    private final TrackingKeywordService service;

    @Operation(summary = "키워드 등록", description = "새 추적 키워드를 등록합니다.")
    @PostMapping
    public ResponseEntity<?> createKeyword(
            @RequestHeader("Authorization") String token,
            @RequestBody AddTrackingKeywordRequest request) {
        String memberId = tokenProvider.getMemberId(token); // JWT에서 사용자 ID 추출
        return ResponseEntity.ok(service.createKeyword(memberId, request));
    }

    @Operation(summary = "키워드 수정", description = "기존 추적 키워드를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateKeyword(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody UpdateTrackingKeywordRequest request) {
        String memberId = tokenProvider.getMemberId(token);
        return ResponseEntity.ok(service.updateKeyword(memberId, id, request));
    }

    @Operation(summary = "키워드 삭제", description = "지정한 추적 키워드를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKeyword
            (@RequestHeader("Authorization") String token,
             @PathVariable Long id) {
        String memberId = tokenProvider.getMemberId(token);
        service.deleteKeyword(memberId, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "키워드 목록 조회", description = "사용자가 등록한 모든 추적 키워드를 반환합니다.")
    @GetMapping
    public ResponseEntity<?> getAllKeyword(@RequestHeader("Authorization") String token) {
        String memberId = tokenProvider.getMemberId(token);
        List<TrackingKeywordResponseDto> keywords = service.getAllKeywords(memberId);
        return ResponseEntity.ok(keywords);
    }

    @Operation(summary = "프로젝트에 속한 키워드 조회", description = "사용자가 등록한 특정 프로젝트의 추적 키워드를 반환합니다.")
    @GetMapping("project/{id}")
    public ResponseEntity<?> getProjectKeyword(@RequestHeader("Authorization") String token,
                                               @PathVariable Long id) {
        String memberId = tokenProvider.getMemberId(token);
        List<TrackingKeywordResponseDto> keywords = service.getProjectKeywords(memberId, id);
        return ResponseEntity.ok(keywords);
    }

    @Operation(summary = "특정 키워드 조회", description = "특정한 추적 키워드의 id를 받아 해당 키워드를 반환합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<?> getTrackingKeyword(@RequestHeader("Authorization") String token ,
                                                @PathVariable Long id) {
        String memberId = tokenProvider.getMemberId(token);
        TrackingKeywordResponseDto keyword = service.getTrackingKeyword(memberId, id);
        return ResponseEntity.ok(keyword);
    }
}