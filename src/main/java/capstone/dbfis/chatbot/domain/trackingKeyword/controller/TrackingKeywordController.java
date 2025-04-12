package capstone.dbfis.chatbot.domain.trackingKeyword.controller;

import capstone.dbfis.chatbot.domain.trackingKeyword.dto.*;
import capstone.dbfis.chatbot.domain.trackingKeyword.service.TrackingKeywordService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking-keywords")
@RequiredArgsConstructor
public class TrackingKeywordController {
    private final TokenProvider tokenProvider;
    private final TrackingKeywordService service;

    @PostMapping
    public ResponseEntity<?> createKeyword(@RequestHeader("Authorization") String token, @RequestBody AddTrackingKeywordRequest request) {
        String memberId = tokenProvider.getMemberId(token); // JWT에서 사용자 ID 추출
        return ResponseEntity.ok(service.createKeyword(memberId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateKeyword(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody UpdateTrackingKeywordRequest request) {
        String memberId = tokenProvider.getMemberId(token);
        return ResponseEntity.ok(service.updateKeyword(memberId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKeyword(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String memberId = tokenProvider.getMemberId(token);
        service.deleteKeyword(memberId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> getAllKeyword(@RequestHeader("Authorization") String token) {
        String memberId = tokenProvider.getMemberId(token);
        return ResponseEntity.ok(service.getAllKeywords(memberId));
    }
}