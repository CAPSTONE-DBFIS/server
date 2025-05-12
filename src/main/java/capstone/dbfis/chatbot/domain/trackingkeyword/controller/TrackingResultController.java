package capstone.dbfis.chatbot.domain.trackingkeyword.controller;

import capstone.dbfis.chatbot.domain.trackingkeyword.dto.TrackingResultResponseDto;
import capstone.dbfis.chatbot.domain.trackingkeyword.service.TrackingResultService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking-results")
@Tag(name = "Tracking Result API", description = "추적 키워드 보고서 API")
@RequiredArgsConstructor
public class TrackingResultController {

    private final TokenProvider tokenProvider;
    private final TrackingResultService trackingResultService;

    @Operation(summary = "추적 결과 조회", description = "특정 키워드에 대한 수집 결과를 반환합니다.")
    @GetMapping("/{keywordId}")
    public ResponseEntity<List<TrackingResultResponseDto>> list(@RequestHeader("Authorization") String token,
                                                                @PathVariable  Long keywordId) {
        // 예시로 tokenProvider를 통해 memberId를 추출하는 로직
        String memberId = tokenProvider.getMemberId(token);

        // 서비스에서 결과 조회
        List<TrackingResultResponseDto> results = trackingResultService.getResultsByKeywordId(memberId, keywordId);

        return ResponseEntity.ok(results);
    }


}
