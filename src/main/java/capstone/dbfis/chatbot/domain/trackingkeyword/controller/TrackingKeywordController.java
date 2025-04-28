package capstone.dbfis.chatbot.domain.trackingkeyword.controller;

import capstone.dbfis.chatbot.domain.trackingkeyword.dto.*;
import capstone.dbfis.chatbot.domain.trackingkeyword.service.TrackingKeywordService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestHeader("Authorization") @NotBlank String token,
            @RequestBody @Valid AddTrackingKeywordRequest request) {
        String memberId = tokenProvider.getMemberId(token); // JWT에서 사용자 ID 추출
        return ResponseEntity.ok(service.createKeyword(memberId, request));
    }

    @Operation(summary = "키워드 수정", description = "기존 추적 키워드를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateKeyword(
            @RequestHeader("Authorization") @NotBlank String token,
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid UpdateTrackingKeywordRequest request) {
        String memberId = tokenProvider.getMemberId(token);
        return ResponseEntity.ok(service.updateKeyword(memberId, id, request));
    }

    @Operation(summary = "키워드 삭제", description = "지정한 추적 키워드를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKeyword
            (@RequestHeader("Authorization") @NotBlank String token,
             @PathVariable @Min(1) Long id) {
        String memberId = tokenProvider.getMemberId(token);
        service.deleteKeyword(memberId, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "키워드 목록 조회", description = "사용자가 등록한 모든 추적 키워드를 반환합니다.")
    @GetMapping
    public ResponseEntity<?> getAllKeyword(@RequestHeader("Authorization") @NotBlank String token) {
        String memberId = tokenProvider.getMemberId(token);
        return ResponseEntity.ok(service.getAllKeywords(memberId));
    }
}