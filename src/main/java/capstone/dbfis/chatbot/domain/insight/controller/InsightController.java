package capstone.dbfis.chatbot.domain.insight.controller;

import capstone.dbfis.chatbot.domain.insight.service.InsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/insight")
@Tag(name = "Insight API", description = "데이터 인사이트 조회 API")
public class InsightController {

    @Autowired
    private InsightService insightService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @GetMapping("/foreign")
    @Operation(summary = "해외 인사이트 데이터 조회", description = "특정 날짜의 해외 종합 인사이트 데이터를 반환합니다.")
    public ResponseEntity<Map<String, Object>> getForeignInsights(@RequestParam("date") String date) {
        return ResponseEntity.ok(insightService.getForeignInsights(date));
    }

    @GetMapping("/foreign/weekly")
    @Operation(summary = "해외 일주일간 인기 키워드 조회", description = "지정된 날짜를 기준으로 최근 7일간의 인기 해외 키워드 및 연관 키워드를 반환합니다.")
    public ResponseEntity<Map<String, Object>> getForeignWeeklyInsights(@RequestParam("date") String date) {
        Map<String, Object> response = insightService.getForeignWeeklyInsights(date);
        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/foreign/related-search")
    @Operation(summary = "해외 연관 검색 기사 조회", description = "키워드 기반의 해외 연관 기사 검색 결과를 반환합니다.")
    public ResponseEntity<String> searchForeignArticles(@RequestParam String keyword, 
                                                        @RequestParam String relatedKeyword,
                                                        @RequestParam String date) {
        String response = insightService.searchForeignByRelatedKeywords(keyword, relatedKeyword, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/foreign/related-search-range")
    @Operation(summary = "해외 관련 키워드로 기간 내 기사 검색", description = "지정된 시작 날짜와 종료 날짜 사이에 주요 키워드 및 관련 키워드를 사용하여 해외 기사를 검색합니다.")
    public ResponseEntity<String> searchForeignRelatedKeywordsRange(@RequestParam("keyword") String keyword,
                                                                  @RequestParam("relatedKeyword") String relatedKeyword,
                                                                  @RequestParam("startDate") String startDate,
                                                                  @RequestParam("endDate") String endDate) {
        String result = insightService.searchForeignByRelatedKeywordsRange(keyword, relatedKeyword, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/foreign/test-db")
    @Operation(summary = "해외 키워드 DB 테스트", description = "해외 키워드 테이블 연결 테스트")
    public ResponseEntity<Map<String, Object>> testForeignKeywordTable() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 해외 키워드 테이블 존재 여부 확인
            List<Map<String, Object>> foreignKeywords = jdbcTemplate.queryForList(
                    "SELECT COUNT(*) as count FROM foreign_keyword");
            response.put("foreign_keyword_count", foreignKeywords.get(0).get("count"));
            
            // 해외 키워드 분석 테이블 존재 여부 확인
            List<Map<String, Object>> foreignAnalysis = jdbcTemplate.queryForList(
                    "SELECT COUNT(*) as count FROM foreign_keyword_analysis");
            response.put("foreign_keyword_analysis_count", foreignAnalysis.get(0).get("count"));
            
            // 가장 최근 날짜 확인
            List<Map<String, Object>> latestDate = jdbcTemplate.queryForList(
                    "SELECT MAX(date) as latest_date FROM foreign_keyword");
            response.put("latest_date", latestDate.get(0).get("latest_date"));
            
            response.put("status", "success");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "키워드 기반 기사 검색", description = "키워드와 카테고리로 기간 내 기사를 검색합니다.")
    public ResponseEntity<String> searchArticlesByKeyword(
            @RequestParam("keyword") String keyword,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "foreign", defaultValue = "false") boolean isForeign) {
        String result = insightService.searchArticlesByKeywordAndCategory(
                keyword, startDate, endDate, category, isForeign);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/categories")
    @Operation(summary = "인기 카테고리 목록 조회", description = "상위 10개 인기 카테고리를 반환합니다.")
    public ResponseEntity<List<Map<String, Object>>> getPopularCategories(
            @RequestParam(value = "foreign", defaultValue = "false") boolean isForeign) {
        List<Map<String, Object>> categories = insightService.getPopularCategories(isForeign);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/sentiment-analysis")
    @Operation(summary = "키워드 기반 기사의 긍부정 비율 분석", description = "키워드로 검색된 기사들의 긍부정 비율을 분석하여 반환합니다.")
    public ResponseEntity<String> getKeywordSentimentAnalysis(
            @RequestParam("keyword") String keyword,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "foreign", defaultValue = "false") boolean isForeign) {
        String result = insightService.getKeywordSentimentAnalysis(
                keyword, startDate, endDate, category, isForeign);
        return ResponseEntity.ok(result);
    }
}