package capstone.dbfis.chatbot.domain.trackingkeyword.controller;

import capstone.dbfis.chatbot.domain.trackingkeyword.service.TrackingResultService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking-results")
@Tag(name = "Tracking Result API", description = "추적 키워드 결과 관리 API")
@RequiredArgsConstructor
public class TrackingResultController {
    private final TokenProvider tokenProvider;
    private final TrackingResultService service;

    @GetMapping("/{keywordId}")
    public ResponseEntity<?> list(@RequestHeader("Authorization") @NotBlank String token,
                                  @PathVariable @Min(1) Long keywordId) {
        String memberId = tokenProvider.getMemberId(token);
        return ResponseEntity.ok(service.getResultsByKeywordId(memberId, keywordId));
    }
}
