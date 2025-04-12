package capstone.dbfis.chatbot.domain.insight.service;

import capstone.dbfis.chatbot.domain.insight.dto.KeywordInsightDto;
import capstone.dbfis.chatbot.domain.insight.dto.WeeklyKeywordInsightDto;
import capstone.dbfis.chatbot.domain.insight.repository.InsightRepository;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import org.json.JSONArray;
import java.util.Arrays;
import org.elasticsearch.common.text.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class InsightService {
    private final RestHighLevelClient client;
    private final String indexName;

    @Autowired
    public InsightService(RestHighLevelClient client, @Value("${elasticsearch.index}") String indexName) {
        this.client = client;
        this.indexName = indexName;
    }

    @Autowired
    private InsightRepository insightRepository;

    public Map<String, Object> getInsights(String date) {
        Map<String, Object> response = new HashMap<>();
        response.put("date", date);
        // 날짜 파싱 부분에서 예외 처리 추가
        if (date != null && !date.isEmpty()) {
            try {
                LocalDate parsedDate = LocalDate.parse(date);  // 원본 파싱 코드
            } catch (DateTimeParseException e) {
                // 날짜가 잘못되었을 때 처리
                System.out.println("Invalid date format: " + date);
            }
        } else {
            // 빈 날짜 값 처리
            System.out.println("Date is empty or null");
        }
        // 상위 10개의 키워드와 연관 키워드 가져오기
        List<KeywordInsightDto> topKeywords = insightRepository.getTopKeywordsByDate(date);
        response.put("top_keywords", topKeywords);

        return response;
    }

    public Map<String, Object> getWeeklyInsights(String date) {
        Map<String, Object> response = new HashMap<>();
        response.put("range_end", date);

        try {
            LocalDate endDate = LocalDate.parse(date);
            LocalDate startDate = endDate.minusDays(6);
            response.put("range_start", startDate.toString());

            // 7일간의 인기 키워드 조회
            List<WeeklyKeywordInsightDto> topWeeklyKeywords = insightRepository.getTopKeywordsInRange(
                    startDate.toString(), endDate.toString());

            response.put("top_weekly_keywords", topWeeklyKeywords);

        } catch (DateTimeParseException e) {
            response.put("error", "Invalid date format. Please use YYYY-MM-DD.");
        }

        return response;
    }

    // 키워드와 연관 키워드가 포함된 뉴스 기사 검색 결과를 Elasticsearch에서 가져오는 함수
    public String searchByRelatedKeywords(String keyword, String relatedKeyword, String date, int page) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);

        int pageSize = 10; // 한 번에 조회할 기사 수
        int from = page * pageSize; // 페이지 번호에 따라 조회 시작 위치 설정

        if (keyword != null && keyword.chars().allMatch(Character::isLetter)) {
            keyword = keyword.toLowerCase();
        }
        if (relatedKeyword != null && relatedKeyword.chars().allMatch(Character::isLetter)) {
            relatedKeyword = relatedKeyword.toLowerCase();
        }

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.boolQuery()
                        .should(QueryBuilders.wildcardQuery("content", "*" + keyword + "*"))
                        .should(QueryBuilders.matchQuery("title", keyword))
                        .minimumShouldMatch(1)
                )
                .must(QueryBuilders.boolQuery()
                        .should(QueryBuilders.wildcardQuery("content", "*" + relatedKeyword + "*"))
                        .should(QueryBuilders.matchQuery("title", relatedKeyword))
                        .minimumShouldMatch(1)
                );


        if (date != null && !date.isEmpty()) {
            boolQuery.filter(QueryBuilders.rangeQuery("date").gte(date).lte(date));
        }

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field(new HighlightBuilder.Field("content"))
                .field(new HighlightBuilder.Field("title"))
                .preTags("<b>")
                .postTags("</b>");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .highlighter(highlightBuilder)
                .from(from) // 페이징 시작 위치 설정
                .size(pageSize); // 한번에 조회할 기사 수 설정

        searchRequest.source(searchSourceBuilder);

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        JSONArray hitsArray = new JSONArray();
        response.getHits().forEach(hit -> {
            JSONObject hitObject = new JSONObject();
            hitObject.put("title", hit.getSourceAsMap().get("title"));
            hitObject.put("media_company", hit.getSourceAsMap().get("media_company"));
            hitObject.put("category", hit.getSourceAsMap().get("category"));
            hitObject.put("url", hit.getSourceAsMap().get("url"));
            hitObject.put("image_url", hit.getSourceAsMap().get("image_url"));

            String rawDate = (String) hit.getSourceAsMap().get("date");
            String formattedDate = formatDateString(rawDate);
            hitObject.put("date", formattedDate);

            JSONArray highlights = new JSONArray();
            if (hit.getHighlightFields().containsKey("content")) {
                Text[] fragments = hit.getHighlightFields().get("content").fragments();
                Arrays.stream(fragments).map(Text::string).forEach(highlights::put);
            }

            if (hit.getHighlightFields().containsKey("title")) {
                Text[] fragments = hit.getHighlightFields().get("title").fragments();
                Arrays.stream(fragments).map(Text::string).forEach(highlights::put);
            }

            hitObject.put("highlights", highlights);
            hitsArray.put(hitObject);
        });

        JSONObject responseObject = new JSONObject();
        responseObject.put("hits", hitsArray);
        responseObject.put("total", response.getHits().getTotalHits().value);

        return responseObject.toString(2);
    }

    public String searchByRelatedKeywordsRange(String keyword, String relatedKeyword, String startDate, String endDate, int page) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);

        int pageSize = 10; // 한 페이지당 조회할 기사 수
        int from = page * pageSize;

        // 알파벳만으로 구성되어 있으면 소문자로 변환
        if (keyword != null && keyword.chars().allMatch(Character::isLetter)) {
            keyword = keyword.toLowerCase();
        }
        if (relatedKeyword != null && relatedKeyword.chars().allMatch(Character::isLetter)) {
            relatedKeyword = relatedKeyword.toLowerCase();
        }

        // 기본 쿼리 구성: 키워드와 연관 키워드를 모두 포함하는 조건
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.boolQuery()
                        .should(QueryBuilders.wildcardQuery("content", "*" + keyword + "*"))
                        .should(QueryBuilders.matchQuery("title", keyword))
                        .minimumShouldMatch(1)
                )
                .must(QueryBuilders.boolQuery()
                        .should(QueryBuilders.wildcardQuery("content", "*" + relatedKeyword + "*"))
                        .should(QueryBuilders.matchQuery("title", relatedKeyword))
                        .minimumShouldMatch(1)
                );

        // 시작일과 종료일이 제공되면 범위로 설정
        if (startDate != null && !startDate.isEmpty() &&
                endDate != null && !endDate.isEmpty()) {
            boolQuery.filter(QueryBuilders.rangeQuery("date").gte(startDate).lte(endDate));
        }

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field(new HighlightBuilder.Field("content"))
                .field(new HighlightBuilder.Field("title"))
                .preTags("<b>")
                .postTags("</b>");

        // SearchSourceBuilder에 sort()를 추가하여 "date" 필드를 내림차순으로 정렬
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .highlighter(highlightBuilder)
                .from(from)
                .size(pageSize)
                .sort("date", SortOrder.DESC);
        searchRequest.source(sourceBuilder);

        // Elasticsearch 검색 실행
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        JSONArray hitsArray = new JSONArray();
        response.getHits().forEach(hit -> {
            JSONObject hitObject = new JSONObject();
            hitObject.put("title", hit.getSourceAsMap().get("title"));
            hitObject.put("media_company", hit.getSourceAsMap().get("media_company"));
            hitObject.put("category", hit.getSourceAsMap().get("category"));
            hitObject.put("url", hit.getSourceAsMap().get("url"));
            hitObject.put("image_url", hit.getSourceAsMap().get("image_url"));

            // 날짜 포맷 변환 (예: "2025-04-04T00:00:00" → "2025-04-04")
            String rawDate = (String) hit.getSourceAsMap().get("date");
            String formattedDate = formatDateString(rawDate);
            hitObject.put("date", formattedDate);

            JSONArray highlights = new JSONArray();
            if (hit.getHighlightFields().containsKey("content")) {
                Text[] fragments = hit.getHighlightFields().get("content").fragments();
                Arrays.stream(fragments).map(Text::string).forEach(highlights::put);
            }
            if (hit.getHighlightFields().containsKey("title")) {
                Text[] fragments = hit.getHighlightFields().get("title").fragments();
                Arrays.stream(fragments).map(Text::string).forEach(highlights::put);
            }
            hitObject.put("highlights", highlights);

            hitsArray.put(hitObject);
        });

        JSONObject responseObject = new JSONObject();
        responseObject.put("hits", hitsArray);
        responseObject.put("total", response.getHits().getTotalHits().value);

        return responseObject.toString(2);
    }

    // 날짜 변환 메서드 추가
    private String formatDateString(String rawDate) {
        try {
            LocalDate date = LocalDate.parse(rawDate.substring(0, 10)); // "2025-04-04T00:00:00" -> "2025-04-04"
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return rawDate; // 변환 실패 시 원본 날짜 반환
        }
    }
}