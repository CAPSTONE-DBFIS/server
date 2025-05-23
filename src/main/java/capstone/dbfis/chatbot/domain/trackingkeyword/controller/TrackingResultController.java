package capstone.dbfis.chatbot.domain.trackingkeyword.controller;

import capstone.dbfis.chatbot.domain.trackingkeyword.dto.*;
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

    @Operation(summary = "추적 llm요약본 조회", description = "추적 키워드에 대한 주기 동안의 수집 요약괍 분석본을 반환합니다.")
    @GetMapping("/{keywordId}")
    public ResponseEntity<List<TrackingResultResponseDto>> report(@RequestHeader("Authorization") String token,
                                                                @PathVariable  Long keywordId) {
        // 예시로 tokenProvider를 통해 memberId를 추출하는 로직
        String memberId = tokenProvider.getMemberId(token);

        // 서비스에서 결과 조회
        List<TrackingResultResponseDto> results = trackingResultService.getResultsByKeywordId(memberId, keywordId);

        return ResponseEntity.ok(results);
    }

    @Operation(summary = "추적 리스트 조회", description = "전체 키워드에 대한 수집 현황을 반환합니다.")
    @GetMapping("/list/{projectId}")
    public ResponseEntity<List<TrackingListResponseDto>> list(@RequestHeader("Authorization") String token,
                                                              @PathVariable  Long projectId) {
        // 예시로 tokenProvider를 통해 memberId를 추출하는 로직
        String memberId = tokenProvider.getMemberId(token);

        // 서비스에서 결과 조회
        List<TrackingListResponseDto> results = trackingResultService.getListByKeywordId(memberId, projectId);

        return ResponseEntity.ok(results);
    }
    @Operation(summary = "추적 리스트에서 업데이트된 연관어 조회", description = "추적 키워드에 대한 연관어 수집 현황을 반환합니다.")
    @GetMapping("/list/{projectId}/related_word")
    public ResponseEntity<List<TrackingListRelatedWordDto>> listRelatedWord(@RequestHeader("Authorization") String token,
                                                              @PathVariable  Long projectId) {
        // 예시로 tokenProvider를 통해 memberId를 추출하는 로직
        String memberId = tokenProvider.getMemberId(token);

        // 서비스에서 결과 조회
        List<TrackingListRelatedWordDto> results = trackingResultService.getListRelatedWord(memberId, projectId);

        return ResponseEntity.ok(results);
    }

    @Operation(summary = "추적 중인 키워드에 대한 기사수와 감정분석 빈도 수 반환", description = "추적 키워드에 대한 기사 수와, 감성분석 수집 결과를 반환합니다.")
    @GetMapping("/{keywordId}/article_counts")
    public ResponseEntity<List<TrackingArticleCountsDto>> article_Counts(@RequestHeader("Authorization") String token,
                                                                                    @PathVariable  Long keywordId) {
        // 예시로 tokenProvider를 통해 memberId를 추출하는 로직
        String memberId = tokenProvider.getMemberId(token);

        // 서비스에서 결과 조회
        List<TrackingArticleCountsDto> results = trackingResultService.parseArticleCounts(memberId, keywordId);

        return ResponseEntity.ok(results);
    }

    @Operation(summary = "추적 중인 키워드에 대한 기사수와 감정분석 빈도 수 반환", description = "추적 키워드에 대한 기사 수와, 감성분석 수집 결과를 반환합니다.")
    @GetMapping("/{keywordId}/sentiments_counts")
    public ResponseEntity<List<TrackingSentimentsDto>> sentiments_Counts(@RequestHeader("Authorization") String token,
                                                                                    @PathVariable  Long keywordId) {
        // 예시로 tokenProvider를 통해 memberId를 추출하는 로직
        String memberId = tokenProvider.getMemberId(token);

        // 서비스에서 결과 조회
        List<TrackingSentimentsDto> results = trackingResultService.parseSentimentsCounts(memberId, keywordId);

        return ResponseEntity.ok(results);
    }

    @Operation(summary = "추적 중인 키워드에 대한 연관어 빈도 수 반환", description = "추적 키워드에 대한 연관어 수집 결과를 반환합니다.")
    @GetMapping("/{keywordId}/related_word_counts")
    public ResponseEntity<List<TrackingRelatedWordsDto>> related_Word_Counts(@RequestHeader("Authorization") String token,
                                                                             @PathVariable  Long keywordId) {
        // 예시로 tokenProvider를 통해 memberId를 추출하는 로직
        String memberId = tokenProvider.getMemberId(token);

        // 서비스에서 결과 조회
        List<TrackingRelatedWordsDto> results = trackingResultService.parseRelatedWordCounts(memberId, keywordId);

        return ResponseEntity.ok(results);
    }

    @Operation(summary = "추적 중인 키워드에 대한 언론사 빈도 수 반환", description = "추적 키워드에 대한 언론사 분석 결과를 반환합니다.")
    @GetMapping("/{keywordId}/media_counts")
    public ResponseEntity<List<TrackingMediaCompanyDto>> media_Counts(@RequestHeader("Authorization") String token,
                                                                             @PathVariable  Long keywordId) {
        // 예시로 tokenProvider를 통해 memberId를 추출하는 로직
        String memberId = tokenProvider.getMemberId(token);

        // 서비스에서 결과 조회
        List<TrackingMediaCompanyDto> results = trackingResultService.parseMediaCounts(memberId, keywordId);

        return ResponseEntity.ok(results);
    }

}
