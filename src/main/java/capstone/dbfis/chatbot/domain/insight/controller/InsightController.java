package capstone.dbfis.chatbot.domain.insight.controller;

import capstone.dbfis.chatbot.domain.insight.service.InsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/insight")
@Tag(name = "Insight API", description = "데이터 인사이트 조회 API")
public class InsightController {

    @Autowired
    private InsightService insightService;

    @GetMapping
    @Operation(summary = "인사이트 데이터 조회", description = "특정 날짜의 종합 인사이트 데이터를 반환합니다.")
    public ResponseEntity<Map<String, Object>> getInsights(@RequestParam("date") String date) {
        return ResponseEntity.ok(insightService.getInsights(date));
    }

    @GetMapping("/weekly")
    @Operation(summary = "일주일간 인기 키워드 조회", description = "지정된 날짜를 기준으로 최근 7일간의 인기 키워드 및 연관 키워드를 반환합니다.")
    public ResponseEntity<Map<String, Object>> getWeeklyInsights(@RequestParam("date") String date) {
        Map<String, Object> response = insightService.getWeeklyInsights(date);
        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/related-search")
    @Operation(summary = "연관 검색 기사 조회", description = "키워드 기반의 연관 기사 검색 결과를 반환합니다.")
    public ResponseEntity<String> searchArticles(@RequestParam String keyword, @RequestParam String relatedKeyword,
                                                 @RequestParam String date) {
        String response = insightService.searchByRelatedKeywords(keyword, relatedKeyword, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/related-search-range")
    @Operation(summary = "관련 키워드로 기간 내 기사 검색", description = "지정된 시작 날짜와 종료 날짜 사이에 주요 키워드 및 관련 키워드를 사용하여 기사를 검색합니다.")
    public ResponseEntity<String> searchRelatedKeywordsRange(@RequestParam("keyword") String keyword,
                                                             @RequestParam("relatedKeyword") String relatedKeyword,
                                                             @RequestParam("startDate") String startDate,
                                                             @RequestParam("endDate") String endDate) {
        String result = insightService.searchByRelatedKeywordsRange(keyword, relatedKeyword, startDate, endDate);
        return ResponseEntity.ok(result);
    }
}
