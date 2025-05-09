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
import java.util.ArrayList;
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

    @Value("${elasticsearch.foreign.index:foreign_news_article}")
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
                                        .match(m -> m.field("title").query(kw))
                                )
                                .must(m2 -> m2
                                        .match(m -> m.field("title").query(rkw))
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
                        .fields("title", ff -> ff)
                        .preTags("<b>")
                        .postTags("</b>")
                )
        );

        SearchResponse<Map> resp;
        try {
            System.out.println("해외 기사 검색 인덱스: " + foreignIndexName);
            System.out.println("검색 파라미터: keyword=" + kw + ", relatedKeyword=" + rkw + ", date=" + dt);
            resp = client.search(req, Map.class);
            System.out.println("검색 결과 개수: " + resp.hits().total().value());
        } catch (IOException ex) {
            System.out.println("해외 뉴스 검색 중 오류 발생: " + ex.getMessage());
            ex.printStackTrace();
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
            if (src == null) {
                System.out.println("검색 결과의 source가 null입니다.");
                continue;
            }
            
            JSONObject obj = new JSONObject(src);
            
            // highlight가 있는 경우에만 추가
            if (hit.highlight() != null && !hit.highlight().isEmpty()) {
                hit.highlight().forEach((field, frags) -> obj.put("highlights", new JSONArray(frags)));
            }
            
            // 날짜 포맷팅
            if (src.containsKey("date") && src.get("date") != null) {
                obj.put("date", formatDateString((String) src.get("date")));
            }
            
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
                                        .match(m -> m.field("title").query(kw))
                                )
                                .must(m2 -> m2
                                        .match(m -> m.field("title").query(rkw))
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
                        .fields("title", ff -> ff)
                        .preTags("<b>")
                        .postTags("</b>")
                )
        );

        SearchResponse<Map> resp;
        try {
            System.out.println("해외 기사 범위 검색 인덱스: " + foreignIndexName);
            System.out.println("검색 파라미터: keyword=" + kw + ", relatedKeyword=" + rkw + 
                               ", startDate=" + startDate + ", endDate=" + endDate);
            resp = client.search(req, Map.class);
            System.out.println("검색 결과 개수: " + resp.hits().total().value());
        } catch (IOException ex) {
            System.out.println("해외 뉴스 검색 중 오류 발생: " + ex.getMessage());
            ex.printStackTrace();
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
            if (src == null) {
                System.out.println("검색 결과의 source가 null입니다.");
                continue;
            }
            
            JSONObject obj = new JSONObject(src);
            
            // highlight가 있는 경우에만 추가
            if (hit.highlight() != null && !hit.highlight().isEmpty()) {
                hit.highlight().forEach((field, frags) -> obj.put("highlights", new JSONArray(frags)));
            }
            
            // 날짜 포맷팅
            if (src.containsKey("date") && src.get("date") != null) {
                obj.put("date", formatDateString((String) src.get("date")));
            }
            
            hits.put(obj);
        }

        JSONObject result = new JSONObject();
        result.put("hits",  hits);
        result.put("total", resp.hits().total().value());
        return result.toString(2);
    }

    /**
     * 키워드 기반 기사 검색 (카테고리 필터링 포함)
     * @param keyword 검색 키워드
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param category 기사 카테고리 (선택)
     * @param isForeign 해외 기사 여부
     * @return 검색 결과 JSON
     */
    public String searchArticlesByKeywordAndCategory(
            String keyword,
            String startDate,
            String endDate,
            String category,
            boolean isForeign
    ) {
        try {
            if (keyword == null || keyword.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색 키워드는 필수입니다.");
            }

            // 날짜 유효성 체크
            if (startDate == null || startDate.isBlank() ||
                    endDate == null || endDate.isBlank()) {
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

            // 검색 인덱스 선택
            String searchIndex = isForeign ? foreignIndexName : indexName;
            System.out.println("검색 인덱스: " + searchIndex);
            System.out.println("검색 파라미터: keyword=" + keyword + ", startDate=" + startDate + 
                               ", endDate=" + endDate + ", category=" + category + ", isForeign=" + isForeign);
            
            // 인덱스 존재 여부 확인
            boolean indexExists = false;
            try {
                indexExists = client.indices().exists(e -> e.index(searchIndex)).value();
                System.out.println("인덱스 존재 여부: " + indexExists);
                if (!indexExists) {
                    System.out.println("검색 인덱스가 존재하지 않습니다: " + searchIndex);
                    // 인덱스가 없으면 빈 결과 반환
                    JSONObject result = new JSONObject();
                    result.put("hits", new JSONArray());
                    result.put("total", 0);
                    result.put("message", "검색 인덱스가 존재하지 않습니다.");
                    return result.toString(2);
                }
            } catch (Exception e) {
                System.out.println("인덱스 확인 중 예외 발생: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 검색 쿼리 빌드
            SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                    .index(searchIndex)
                    .query(q -> {
                        // 기본 bool 쿼리 시작
                        co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder boolBuilder = new co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder();
                        
                        // 키워드 검색 조건 추가 - 제목(title)에서만 검색하도록 수정
                        boolBuilder.must(s -> s.match(m -> m.field("title").query(keyword)));
                        
                        // 날짜 범위 필터 추가
                        boolBuilder.filter(f -> f.range(r -> r
                                .field("date")
                                .gte(JsonData.of(startDate))
                                .lte(JsonData.of(endDate))
                        ));
                        
                        // 카테고리 필터 추가 (값이 있는 경우에만)
                        if (category != null && !category.isBlank()) {
                            boolBuilder.filter(f -> f.term(t -> t
                                    .field("category")
                                    .value(category.toLowerCase())
                            ));
                        }
                        
                        return q.bool(boolBuilder.build());
                    })
                    .sort(s -> s.field(f -> f.field("date").order(SortOrder.Desc)))
                    .size(20)  // 결과 수 제한
                    .highlight(h -> h
                            .fields("title", ff -> ff)
                            .preTags("<b>")
                            .postTags("</b>")
                    );

            SearchResponse<Map> resp;
            try {
                SearchRequest searchRequest = searchBuilder.build();
                System.out.println("실행할 검색 쿼리: " + searchRequest.toString());
                resp = client.search(searchRequest, Map.class);
                System.out.println("검색 결과 개수: " + resp.hits().total().value());
            } catch (IOException ex) {
                System.out.println("뉴스 검색 중 IO 오류 발생: " + ex.getMessage());
                ex.printStackTrace();
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "뉴스 검색 중 오류가 발생했습니다.",
                        ex
                );
            }

            JSONArray hits = new JSONArray();
            try {
                for (Hit<Map> hit : resp.hits().hits()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> src = (Map<String, Object>) hit.source();
                    if (src == null) {
                        System.out.println("검색 결과의 source가 null입니다.");
                        continue;
                    }
                    
                    JSONObject obj = new JSONObject(src);
                    
                    // highlight가 있는 경우에만 추가
                    if (hit.highlight() != null && !hit.highlight().isEmpty()) {
                        hit.highlight().forEach((field, frags) -> obj.put("highlights", new JSONArray(frags)));
                    }
                    
                    // 날짜 포맷팅
                    if (src.containsKey("date") && src.get("date") != null) {
                        obj.put("date", formatDateString((String) src.get("date")));
                    }
                    
                    // 카테고리가 없을 경우 "일반" 카테고리로 설정
                    if (!obj.has("category") || obj.isNull("category")) {
                        obj.put("category", "일반");
                    }
                    
                    // 하이라이트 정보 추가
                    if (hit.highlight() != null && hit.highlight().containsKey("title")) {
                        obj.put("highlights", new JSONArray(hit.highlight().get("title")));
                    }
                    
                    // 긍부정 정보 추가
                    if (src.get("sentiment") != null) {
                        obj.put("sentiment", src.get("sentiment"));
                    }
                    
                    hits.put(obj);
                }
            } catch (Exception e) {
                System.out.println("검색 결과 처리 중 예외 발생: " + e.getMessage());
                e.printStackTrace();
            }

            JSONObject result = new JSONObject();
            result.put("hits", hits);
            result.put("total", resp.hits().total().value());
            return result.toString(2);
        } catch (ResponseStatusException ex) {
            // 이미 처리된 예외는 그대로 전달
            throw ex;
        } catch (Exception ex) {
            // 예상치 못한 예외는 로깅 후 500 에러로 변환
            System.out.println("검색 처리 중 예상치 못한 예외 발생: " + ex.getMessage());
            ex.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "서버 처리 중 오류가 발생했습니다.",
                    ex
            );
        }
    }
    
    /**
     * 인기 카테고리 목록 조회 (상위 10개)
     * @param isForeign 해외 기사 여부
     * @return 카테고리 목록
     */
    public List<Map<String, Object>> getPopularCategories(boolean isForeign) {
        String searchIndex = isForeign ? foreignIndexName : indexName;
        System.out.println("카테고리 조회 인덱스: " + searchIndex);
        
        try {
            // 인덱스 존재 여부 확인
            boolean indexExists = client.indices().exists(e -> e.index(searchIndex)).value();
            if (!indexExists) {
                System.out.println("카테고리 조회: 인덱스가 존재하지 않습니다: " + searchIndex);
                // 인덱스가 없으면 빈 리스트 반환
                return new ArrayList<>();
            }
            
            SearchResponse<Map> response = client.search(s -> s
                    .index(searchIndex)
                    .size(0) // 실제 문서는 필요 없고 집계만 필요
                    .aggregations("categories", a -> a
                            .terms(t -> t
                                    .field("category")
                                    .size(10)
                            )
                    ), 
                    Map.class
            );
            
            List<Map<String, Object>> categories = new ArrayList<>();
            
            // 결과가 null이 아닌지 확인
            if (response.aggregations() != null && 
                response.aggregations().get("categories") != null &&
                response.aggregations().get("categories").sterms() != null &&
                response.aggregations().get("categories").sterms().buckets() != null &&
                response.aggregations().get("categories").sterms().buckets().array() != null) {
                
                response.aggregations().get("categories").sterms().buckets().array().forEach(b -> {
                    Map<String, Object> category = new HashMap<>();
                    // null이거나 비어있는 경우 "일반" 카테고리로 표시
                    String categoryName = b.key().stringValue();
                    if (categoryName == null || categoryName.isBlank()) {
                        categoryName = "일반";
                    }
                    category.put("name", categoryName);
                    category.put("count", b.docCount());
                    categories.add(category);
                });
            } else {
                System.out.println("카테고리 조회: 집계 결과가 비어있습니다.");
                // 기본 카테고리 추가
                Map<String, Object> defaultCategory = new HashMap<>();
                defaultCategory.put("name", "일반");
                defaultCategory.put("count", 0);
                categories.add(defaultCategory);
            }
            
            System.out.println("카테고리 조회 결과: " + categories.size() + "개");
            
            return categories;
        } catch (IOException e) {
            System.out.println("카테고리 목록 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "카테고리 목록 조회 중 오류가 발생했습니다.",
                    e
            );
        }
    }

    /**
     * 키워드 검색 결과의 긍/부정 비율 분석
     * @param keyword 검색 키워드
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param category 기사 카테고리 (선택)
     * @param isForeign 해외 기사 여부 
     * @return 긍/부정 비율 분석 결과 JSON
     */
    public String getKeywordSentimentAnalysis(
            String keyword,
            String startDate,
            String endDate,
            String category,
            boolean isForeign
    ) {
        try {
            if (keyword == null || keyword.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색 키워드는 필수입니다.");
            }

            // 날짜 유효성 체크
            if (startDate == null || startDate.isBlank() ||
                    endDate == null || endDate.isBlank()) {
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

            // 검색 인덱스 선택
            String searchIndex = isForeign ? foreignIndexName : indexName;
            System.out.println("감정분석 검색 인덱스: " + searchIndex);
            System.out.println("감정분석 파라미터: keyword=" + keyword + ", startDate=" + startDate + 
                               ", endDate=" + endDate + ", category=" + category + ", isForeign=" + isForeign);
            
            // 인덱스 존재 여부 확인
            boolean indexExists = false;
            try {
                indexExists = client.indices().exists(e -> e.index(searchIndex)).value();
                if (!indexExists) {
                    System.out.println("검색 인덱스가 존재하지 않습니다: " + searchIndex);
                    // 인덱스가 없으면 빈 결과 반환
                    JSONObject result = new JSONObject();
                    result.put("message", "검색 인덱스가 존재하지 않습니다.");
                    result.put("positive", 0);
                    result.put("negative", 0);
                    result.put("neutral", 0);
                    result.put("total", 0);
                    return result.toString(2);
                }
            } catch (Exception e) {
                System.out.println("인덱스 확인 중 예외 발생: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 검색 쿼리 빌드 - 집계(aggregation) 포함
            SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                    .index(searchIndex)
                    .size(0) // 실제 검색 결과는 필요 없이 집계만 필요
                    .query(q -> {
                        // 기본 bool 쿼리 시작
                        co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder boolBuilder = 
                            new co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder();
                        
                        // 키워드 검색 조건 추가 - 제목에서 검색
                        boolBuilder.must(s -> s.match(m -> m.field("title").query(keyword)));
                        
                        // 날짜 범위 필터 추가
                        boolBuilder.filter(f -> f.range(r -> r
                                .field("date")
                                .gte(JsonData.of(startDate))
                                .lte(JsonData.of(endDate))
                        ));
                        
                        // 카테고리 필터 추가 (값이 있는 경우에만)
                        if (category != null && !category.isBlank()) {
                            boolBuilder.filter(f -> f.term(t -> t
                                    .field("category")
                                    .value(category.toLowerCase())
                            ));
                        }
                        
                        return q.bool(boolBuilder.build());
                    })
                    .aggregations("sentiment_counts", a -> a
                            .terms(t -> t
                                    .field("sentiment")
                                    .size(10) // 충분히 큰 사이즈
                            )
                    );

            SearchResponse<Map> resp;
            try {
                SearchRequest searchRequest = searchBuilder.build();
                resp = client.search(searchRequest, Map.class);
                System.out.println("감정분석 검색 결과의 총 개수: " + resp.hits().total().value());
            } catch (IOException ex) {
                System.out.println("감정분석 검색 중 IO 오류 발생: " + ex.getMessage());
                ex.printStackTrace();
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "감정분석 검색 중 오류가 발생했습니다.",
                        ex
                );
            }

            // 감정 분석 결과 처리
            JSONObject result = new JSONObject();
            long positive = 0;
            long negative = 0;
            long neutral = 0;
            long total = resp.hits().total().value();
            
            // 집계 결과가 존재하는지 확인
            if (resp.aggregations() != null && 
                resp.aggregations().get("sentiment_counts") != null &&
                resp.aggregations().get("sentiment_counts").sterms() != null &&
                resp.aggregations().get("sentiment_counts").sterms().buckets() != null &&
                resp.aggregations().get("sentiment_counts").sterms().buckets().array() != null) {
                
                // 감정별 집계 처리
                for (var bucket : resp.aggregations().get("sentiment_counts").sterms().buckets().array()) {
                    String sentimentValue = bucket.key().stringValue();
                    long count = bucket.docCount();
                    
                    if ("positive".equals(sentimentValue)) {
                        positive = count;
                    } else if ("negative".equals(sentimentValue)) {
                        negative = count;
                    } else if ("neutral".equals(sentimentValue)) {
                        neutral = count;
                    }
                }
            }
            
            // 결과 JSON 구성
            result.put("positive", positive);
            result.put("negative", negative);
            result.put("neutral", neutral);
            result.put("total", total);
            
            // 백분율 계산 (전체 문서가 0개가 아닌 경우만)
            if (total > 0) {
                result.put("positive_percent", Math.round((positive * 100.0) / total));
                result.put("negative_percent", Math.round((negative * 100.0) / total));
                result.put("neutral_percent", Math.round((neutral * 100.0) / total));
            } else {
                result.put("positive_percent", 0);
                result.put("negative_percent", 0);
                result.put("neutral_percent", 0);
            }
            
            return result.toString(2);
            
        } catch (ResponseStatusException ex) {
            // 이미 처리된 예외는 그대로 전달
            throw ex;
        } catch (Exception ex) {
            // 예상치 못한 예외는 로깅 후 500 에러로 변환
            System.out.println("감정분석 처리 중 예상치 못한 예외 발생: " + ex.getMessage());
            ex.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "서버 처리 중 오류가 발생했습니다.",
                    ex
            );
        }
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