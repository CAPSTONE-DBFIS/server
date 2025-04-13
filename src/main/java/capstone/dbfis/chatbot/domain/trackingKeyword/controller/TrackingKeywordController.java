package capstone.dbfis.chatbot.domain.trackingKeyword.controller;

import capstone.dbfis.chatbot.domain.trackingKeyword.dto.*;
import capstone.dbfis.chatbot.domain.trackingKeyword.service.TrackingKeywordService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking-keywords")
@RequiredArgsConstructor
@Tag(name = "TrackingKeyword API", description = "추적 키워드 관리 API")
public class TrackingKeywordController {

    private final TrackingKeywordService trackingKeywordService;
    private final TokenProvider tokenProvider;

    @Operation(summary = "모든 추적 키워드 조회", description = "사용자가 요청한 모든 추적 키워드와 결과를 조회합니다.")
    @GetMapping
    public List<TrackingKeywordResponse> getAllKeywordsAndResultsByRequesterId(@RequestHeader("Authorization") String token) {
        String requesterId = tokenProvider.getMemberId(token);
        return trackingKeywordService.getAllKeywordsAndResultsByRequesterId(requesterId);
    }

    @Operation(summary = "추적 키워드 추가", description = "새로운 추적 키워드를 추가합니다.")
    @PostMapping
    public TrackingKeywordResponse addTrackingKeyword(@RequestHeader("Authorization") String token, @RequestBody AddTrackingKeywordRequest request) {
        String requesterId = tokenProvider.getMemberId(token);
        return trackingKeywordService.addTrackingKeyword(requesterId, request);
    }

    @Operation(summary = "추적 키워드 삭제", description = "특정 추적 키워드를 삭제합니다.")
    @DeleteMapping("/{keywordId}")
    public void deleteTrackingKeyword(@RequestHeader("Authorization") String token, @PathVariable Long keywordId) {
        String requesterId = tokenProvider.getMemberId(token);
        trackingKeywordService.deleteTrackingKeyword(requesterId, keywordId);
    }

    @Operation(summary = "추적 키워드 수정", description = "특정 추적 키워드의 날짜를 수정합니다.")
    @PatchMapping("/{keywordId}")
    public TrackingKeywordResponse updateTrackingKeywordDate(
            @RequestHeader("Authorization") String token,
            @PathVariable Long keywordId,
            @RequestBody UpdateTrackingKeywordRequest request) {

        String requesterId = tokenProvider.getMemberId(token);
        return trackingKeywordService.updateTrackingKeywordDate(requesterId, keywordId, request);
    }
}