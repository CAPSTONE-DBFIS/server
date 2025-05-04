package capstone.dbfis.chatbot.domain.insight.service;

import capstone.dbfis.chatbot.domain.insight.dto.KeywordInsightDto;
import capstone.dbfis.chatbot.domain.insight.dto.WeeklyKeywordInsightDto;
import capstone.dbfis.chatbot.domain.insight.repository.InsightRepository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch를 이용해 키워드 인사이트를 제공하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class InsightService {
    private final ElasticsearchClient client;
    private final InsightRepository insightRepository;

    @Value("${elasticsearch.index}")
    private String indexName;

    @Value("${elasticsearch.foreign.index:foreign_news}")
    private String foreignIndexName;

    /**
     * 단일 날짜를 기준으로 상위 키워드 인사이트를 반환
     */
    public Map<String, Object> getInsights(String date) {
        if (date == null || date.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "날짜를 입력해야 합니다.");
        }

        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "날짜 형식이 잘못되었습니다. YYYY-MM-DD 형식으로 입력해주세요."
            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("date", date);

        // 저장소에서 상위 키워드 조회
        List<KeywordInsightDto> topKeywords = insightRepository.getTopKeywordsByDate(date);
        response.put("top_keywords", topKeywords);

        return response;
    }

    /**
     * 주어진 날짜를 끝으로 7일간의 주간 키워드 인사이트를 반환
     */
    public Map<String, Object> getWeeklyInsights(String date) {
        if (date == null || date.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "날짜를 입력해야 합니다.");
        }

        LocalDate endDate;
        try {
            endDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "날짜 형식이 잘못되었습니다. YYYY-MM-DD 형식으로 입력해주세요."
            );
        }

        LocalDate startDate = endDate.minusDays(6);
        Map<String, Object> response = new HashMap<>();
        response.put("range_start", startDate.toString());
        response.put("range_end", date);

        // 7일간 인기 키워드 조회
        List<WeeklyKeywordInsightDto> weekly =
                insightRepository.getTopKeywordsInRange(startDate.toString(), date);
        response.put("top_weekly_keywords", weekly);

        return response;
    }

    /**
     * 키워드와 연관 키워드가 포함된 뉴스 기사 검색
     * 프론트엔드에서 페이징 처리하도록 from/size 파라미터 제거
     */
    public String searchByRelatedKeywords(
            String keyword,
            String relatedKeyword,
            String date
    ) {
        final String kw = (keyword != null && keyword.chars().allMatch(Character::isLetter))
                ? keyword.toLowerCase()
                : keyword;
        final String rkw = (relatedKeyword != null && relatedKeyword.chars().allMatch(Character::isLetter))
                ? relatedKeyword.toLowerCase()
                : relatedKeyword;
        final String dt = date;

        // 날짜 유효성 체크
        if (dt == null || dt.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "날짜를 입력해야 합니다.");
        }
        try {
            LocalDate.parse(dt);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "날짜 형식이 잘못되었습니다. YYYY-MM-DD 형식으로 입력해주세요."
            );
        }

        SearchRequest req = SearchRequest.of(b -> b
                .index(indexName)
                .query(q -> q
                        .bool(bb -> bb
                                .must(m1 -> m1
                                        .bool(inner -> inner
                                                .should(s -> s.wildcard(w -> w.field("content").value("*" + kw + "*")))
                                                .should(s -> s.match(mt -> mt.field("title").query(kw)))
                                                .minimumShouldMatch("1")
                                        )
                                )
                                .must(m2 -> m2
                                        .bool(inner -> inner
                                                .should(s -> s.wildcard(w -> w.field("content").value("*" + rkw + "*")))
                                                .should(s -> s.match(mt -> mt.field("title").query(rkw)))
                                                .minimumShouldMatch("1")
                                        )
                                )
                                .filter(f -> f
                                        .range(r -> r
                                                .field("date")
                                                .gte(JsonData.of(dt))
                                                .lte(JsonData.of(dt))
                                        )
                                )
                        )
                )
                .sort(s -> s.field(f -> f.field("date").order(SortOrder.Desc)))
                .highlight(h -> h
                        .fields("content", ff -> ff)
                        .fields("title",   ff -> ff)
                        .preTags("<b>")
                        .postTags("</b>")
                )
        );

        SearchResponse<Map> resp;
        try {
            resp = client.search(req, Map.class);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "뉴스 검색 중 오류가 발생했습니다.",
                    ex
            );
        }

        JSONArray hits = new JSONArray();
        for (Hit<Map> hit : resp.hits().hits()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> src = (Map<String, Object>) hit.source();
            JSONObject obj = new JSONObject(src);
            hit.highlight()
                    .forEach((field, frags) -> obj.put("highlights", new JSONArray(frags)));
            obj.put("date", formatDateString((String) src.get("date")));
            hits.put(obj);
        }

        JSONObject result = new JSONObject();
        result.put("hits",  hits);
        result.put("total", resp.hits().total().value());
        return result.toString(2);
    }

    /**
     * 키워드 범위 검색 (시작/종료일)
     * 프론트엔드에서 페이징 처리하도록 from/size 파라미터 제거
     */
    public String searchByRelatedKeywordsRange(
            String keyword,
            String relatedKeyword,
            String startDate,
            String endDate
    ) {
        final String kw = (keyword != null && keyword.chars().allMatch(Character::isLetter))
                ? keyword.toLowerCase()
                : keyword;
        final String rkw = (relatedKeyword != null && relatedKeyword.chars().allMatch(Character::isLetter))
                ? relatedKeyword.toLowerCase()
                : relatedKeyword;

        // 날짜 유효성 체크
        if (startDate == null || startDate.isBlank() ||
                endDate   == null || endDate.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "시작일과 종료일을 모두 입력해야 합니다."
            );
        }
        try {
            LocalDate.parse(startDate);
            LocalDate.parse(endDate);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "날짜 형식이 잘못되었습니다. YYYY-MM-DD 형식으로 입력해주세요."
            );
        }

        SearchRequest req = SearchRequest.of(b -> b
                .index(indexName)
                .query(q -> q
                        .bool(bb -> bb
                                .must(m1 -> m1
                                        .bool(inner -> inner
                                                .should(s -> s.wildcard(w -> w.field("content").value("*" + kw + "*")))
                                                .should(s -> s.match(mt -> mt.field("title").query(kw)))
                                                .minimumShouldMatch("1")
                                        )
                                )
                                .must(m2 -> m2
                                        .bool(inner -> inner
                                                .should(s -> s.wildcard(w -> w.field("content").value("*" + rkw + "*")))
                                                .should(s -> s.match(mt -> mt.field("title").query(rkw)))
                                                .minimumShouldMatch("1")
                                        )
                                )
                                .filter(f -> f
                                        .range(r -> r
                                                .field("date")
                                                .gte(JsonData.of(startDate))
                                                .lte(JsonData.of(endDate))
                                        )
                                )
                        )
                )
                .sort(s -> s.field(f -> f.field("date").order(SortOrder.Desc)))
                .highlight(h -> h
                        .fields("content", ff -> ff)
                        .fields("title",   ff -> ff)
                        .preTags("<b>")
                        .postTags("</b>")
                )
        );

        SearchResponse<Map> resp;
        try {
            resp = client.search(req, Map.class);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "뉴스 검색 중 오류가 발생했습니다.",
                    ex
            );
        }

        JSONArray hits = new JSONArray();
        for (Hit<Map> hit : resp.hits().hits()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> src = (Map<String, Object>) hit.source();
            JSONObject obj = new JSONObject(src);
            hit.highlight()
                    .forEach((field, frags) -> obj.put("highlights", new JSONArray(frags)));
            obj.put("date", formatDateString((String) src.get("date")));
            hits.put(obj);
        }

        JSONObject result = new JSONObject();
        result.put("hits",  hits);
        result.put("total", resp.hits().total().value());
        return result.toString(2);
    }

    /**
     * 해외 키워드 인사이트 데이터 조회
     */
    public Map<String, Object> getForeignInsights(String date) {
        if (date == null || date.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "날짜를 입력해야 합니다.");
        }

        try {
            LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "날짜 형식이 잘못되었습니다. YYYY-MM-DD 형식으로 입력해주세요."
            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("date", date);

        // 저장소에서 해외 상위 키워드 조회
        List<KeywordInsightDto> topForeignKeywords = insightRepository.getTopForeignKeywordsByDate(date);
        response.put("top_foreign_keywords", topForeignKeywords);

        return response;
    }
    
    /**
     * 해외 주간 키워드 인사이트 데이터 조회
     */
    public Map<String, Object> getForeignWeeklyInsights(String date) {
        if (date == null || date.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "날짜를 입력해야 합니다.");
        }

        LocalDate endDate;
        try {
            endDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "날짜 형식이 잘못되었습니다. YYYY-MM-DD 형식으로 입력해주세요."
            );
        }

        LocalDate startDate = endDate.minusDays(6);
        Map<String, Object> response = new HashMap<>();
        response.put("range_start", startDate.toString());
        response.put("range_end", date);

        // 7일간 해외 인기 키워드 조회
        List<WeeklyKeywordInsightDto> weeklyForeign =
                insightRepository.getTopForeignKeywordsInRange(startDate.toString(), date);
        response.put("top_weekly_foreign_keywords", weeklyForeign);

        return response;
    }
    
    /**
     * 해외 키워드와 연관 키워드가 포함된 뉴스 기사 검색
     */
    public String searchForeignByRelatedKeywords(
            String keyword,
            String relatedKeyword,
            String date
    ) {
        final String kw = (keyword != null && keyword.chars().allMatch(Character::isLetter))
                ? keyword.toLowerCase()
                : keyword;
        final String rkw = (relatedKeyword != null && relatedKeyword.chars().allMatch(Character::isLetter))
                ? relatedKeyword.toLowerCase()
                : relatedKeyword;
        final String dt = date;

        // 날짜 유효성 체크
        if (dt == null || dt.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "날짜를 입력해야 합니다.");
        }
        try {
            LocalDate.parse(dt);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "날짜 형식이 잘못되었습니다. YYYY-MM-DD 형식으로 입력해주세요."
            );
        }

        SearchRequest req = SearchRequest.of(b -> b
                .index(foreignIndexName)  // 해외 뉴스 인덱스 사용
                .query(q -> q
                        .bool(bb -> bb
                                .must(m1 -> m1
                                        .bool(inner -> inner
                                                .should(s -> s.wildcard(w -> w.field("content").value("*" + kw + "*")))
                                                .should(s -> s.match(mt -> mt.field("title").query(kw)))
                                                .minimumShouldMatch("1")
                                        )
                                )
                                .must(m2 -> m2
                                        .bool(inner -> inner
                                                .should(s -> s.wildcard(w -> w.field("content").value("*" + rkw + "*")))
                                                .should(s -> s.match(mt -> mt.field("title").query(rkw)))
                                                .minimumShouldMatch("1")
                                        )
                                )
                                .filter(f -> f
                                        .range(r -> r
                                                .field("date")
                                                .gte(JsonData.of(dt))
                                                .lte(JsonData.of(dt))
                                        )
                                )
                        )
                )
                .sort(s -> s.field(f -> f.field("date").order(SortOrder.Desc)))
                .highlight(h -> h
                        .fields("content", ff -> ff)
                        .fields("title",   ff -> ff)
                        .preTags("<b>")
                        .postTags("</b>")
                )
        );

        SearchResponse<Map> resp;
        try {
            resp = client.search(req, Map.class);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "해외 뉴스 검색 중 오류가 발생했습니다.",
                    ex
            );
        }

        JSONArray hits = new JSONArray();
        for (Hit<Map> hit : resp.hits().hits()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> src = (Map<String, Object>) hit.source();
            JSONObject obj = new JSONObject(src);
            hit.highlight()
                    .forEach((field, frags) -> obj.put("highlights", new JSONArray(frags)));
            obj.put("date", formatDateString((String) src.get("date")));
            hits.put(obj);
        }

        JSONObject result = new JSONObject();
        result.put("hits",  hits);
        result.put("total", resp.hits().total().value());
        return result.toString(2);
    }
    
    /**
     * 키워드 범위 검색 (시작/종료일) - 해외 기사
     */
    public String searchForeignByRelatedKeywordsRange(
            String keyword,
            String relatedKeyword,
            String startDate,
            String endDate
    ) {
        // 키워드 및 날짜 유효성 검사 및 처리
        if (keyword == null || keyword.isBlank() || relatedKeyword == null || relatedKeyword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "키워드와 관련 키워드가 필요합니다.");
        }

        try {
            LocalDate.parse(startDate);
            LocalDate.parse(endDate);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "날짜 형식이 잘못되었습니다.");
        }

        final String kw = keyword.chars().allMatch(Character::isLetter)
                ? keyword.toLowerCase()
                : keyword;
        final String rkw = relatedKeyword.chars().allMatch(Character::isLetter)
                ? relatedKeyword.toLowerCase()
                : relatedKeyword;

        SearchRequest req = SearchRequest.of(b -> b
                .index(foreignIndexName)  // 해외 뉴스 인덱스 사용
                .query(q -> q
                        .bool(bb -> bb
                                .must(m1 -> m1
                                        .bool(inner -> inner
                                                .should(s -> s.wildcard(w -> w.field("content").value("*" + kw + "*")))
                                                .should(s -> s.match(mt -> mt.field("title").query(kw)))
                                                .minimumShouldMatch("1")
                                        )
                                )
                                .must(m2 -> m2
                                        .bool(inner -> inner
                                                .should(s -> s.wildcard(w -> w.field("content").value("*" + rkw + "*")))
                                                .should(s -> s.match(mt -> mt.field("title").query(rkw)))
                                                .minimumShouldMatch("1")
                                        )
                                )
                                .filter(f -> f
                                        .range(r -> r
                                                .field("date")
                                                .gte(JsonData.of(startDate))
                                                .lte(JsonData.of(endDate))
                                        )
                                )
                        )
                )
                .sort(s -> s.field(f -> f.field("date").order(SortOrder.Desc)))
                .highlight(h -> h
                        .fields("content", ff -> ff)
                        .fields("title",   ff -> ff)
                        .preTags("<b>")
                        .postTags("</b>")
                )
        );

        SearchResponse<Map> resp;
        try {
            resp = client.search(req, Map.class);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "해외 뉴스 검색 중 오류가 발생했습니다.",
                    ex
            );
        }

        JSONArray hits = new JSONArray();
        for (Hit<Map> hit : resp.hits().hits()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> src = (Map<String, Object>) hit.source();
            JSONObject obj = new JSONObject(src);
            hit.highlight()
                    .forEach((field, frags) -> obj.put("highlights", new JSONArray(frags)));
            obj.put("date", formatDateString((String) src.get("date")));
            hits.put(obj);
        }

        JSONObject result = new JSONObject();
        result.put("hits",  hits);
        result.put("total", resp.hits().total().value());
        return result.toString(2);
    }

    // 날짜 문자열을 yyyy-MM-dd 형식으로 변환
    private String formatDateString(String rawDate) {
        try {
            LocalDate d = LocalDate.parse(rawDate.substring(0, 10));
            return d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return rawDate;
        }
    }
}