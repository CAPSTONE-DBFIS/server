package capstone.dbfis.chatbot.domain.insight.controller;

import capstone.dbfis.chatbot.domain.insight.service.InsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/insight")
@Tag(name = "Insight", description = "데이터 인사이트 조회 API")
public class InsightController {

    @Autowired
    private InsightService insightService;

    @GetMapping
    @Operation(
            summary = "인사이트 데이터 조회",
            description = "특정 날짜의 종합 인사이트 데이터를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 데이터 조회 완료"),
                    @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식")
            }
    )
    public ResponseEntity<Map<String, Object>> getInsights(
            @Parameter(
                    name = "date",
                    description = "조회 날짜 (YYYY-MM-DD 형식)",
                    example = "2025-04-08",
                    required = true
            )
            @RequestParam("date") String date
    ) {
        return ResponseEntity.ok(insightService.getInsights(date));
    }

    @GetMapping("/weekly")
    @Operation(
            summary = "일주일간 인기 키워드 조회",
            description = "지정된 날짜를 기준으로 최근 7일간의 인기 키워드 및 연관 키워드를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 데이터 조회 완료"),
                    @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식")
            }
    )
    public ResponseEntity<Map<String, Object>> getWeeklyInsights(
            @Parameter(
                    name = "date",
                    description = "기준 날짜 (YYYY-MM-DD 형식). 해당 날짜를 포함하여 7일간 집계합니다.",
                    example = "2025-04-08",
                    required = true
            )
            @RequestParam("date") String date
    ) {
        Map<String, Object> response = insightService.getWeeklyInsights(date);
        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/related-search")
    @Operation(
            summary = "연관 검색 기사 조회",
            description = "키워드 기반의 연관 기사 검색 결과를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 검색 완료"),
                    @ApiResponse(responseCode = "400", description = "필수 파라미터 누락"),
                    @ApiResponse(responseCode = "500", description = "외부 API 연결 오류")
            }
    )

    public ResponseEntity<String> searchArticles(
            @Parameter(name = "keyword", description = "주 검색 키워드", example = "AI", required = true)
            @RequestParam String keyword,

            @Parameter(name = "relatedKeyword", description = "연관 검색 키워드", example = "삼성", required = true)
            @RequestParam String relatedKeyword,

            @Parameter(name = "date", description = "검색 기준일 (YYYY-MM-DD)", example = "2025-04-08", required = true)
            @RequestParam String date,

            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0", required = false)
            @RequestParam(required = false, defaultValue = "0") int page
    ) throws IOException {
        String response = insightService.searchByRelatedKeywords(keyword, relatedKeyword, date, page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/related-search-range")
    @Operation(
            summary = "Search articles by related keywords with date range",
            description = "Search articles using main and related keywords between the specified startDate and endDate.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved articles"),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters or date range")
            }
    )
    public ResponseEntity<String> searchRelatedKeywordsRange(
            @RequestParam("keyword") String keyword,
            @RequestParam("relatedKeyword") String relatedKeyword,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) throws IOException {
        try {
            String result = insightService.searchByRelatedKeywordsRange(keyword, relatedKeyword, startDate, endDate, page);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
