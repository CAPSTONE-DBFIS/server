package capstone.dbfis.chatbot.domain.trackingKeyword.controller;

import capstone.dbfis.chatbot.domain.trackingKeyword.dto.*;
import capstone.dbfis.chatbot.domain.trackingKeyword.service.TrackingKeywordService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking-keywords")
@RequiredArgsConstructor
public class TrackingKeywordController {

    private final TrackingKeywordService trackingKeywordService;
    private final TokenProvider tokenProvider;

    // 사용자가 요청한 모든 추적 키워드와 결과 조회
    @GetMapping
    public List<TrackingKeywordResponse> getAllKeywordsAndResultsByRequesterId(@RequestHeader("Authorization") String token) {
        String requesterId = tokenProvider.getMemberId(token);
        return trackingKeywordService.getAllKeywordsAndResultsByRequesterId(requesterId);
    }

    // 키워드 추가
    @PostMapping
    public TrackingKeywordResponse addTrackingKeyword(@RequestHeader("Authorization") String token, @RequestBody AddTrackingKeywordRequest request) {
        String requesterId = tokenProvider.getMemberId(token);
        return trackingKeywordService.addTrackingKeyword(requesterId, request);
    }
}