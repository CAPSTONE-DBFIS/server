package capstone.dbfis.chatbot.domain.trackingKeyword.controller;

import capstone.dbfis.chatbot.domain.trackingKeyword.service.TrackingResultService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking-results")
@RequiredArgsConstructor
public class TrackingResultController {
    private final TokenProvider tokenProvider;
    private final TrackingResultService service;

    @GetMapping("/{keywordId}")
    public ResponseEntity<?> list(@RequestHeader("Authorization") String token, @PathVariable Long keywordId) {
        String memberId = tokenProvider.getMemberId(token);
        return ResponseEntity.ok(service.getResultsByKeywordId(memberId, keywordId));
    }
}
