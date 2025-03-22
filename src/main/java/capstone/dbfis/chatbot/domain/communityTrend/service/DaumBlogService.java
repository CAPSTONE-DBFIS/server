package capstone.dbfis.chatbot.domain.communityTrend.service;

import capstone.dbfis.chatbot.domain.communityTrend.dto.DaumResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DaumBlogService {

    @Value("${kakao.api.url}")
    private String apiUrl;

    @Value("${kakao.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public List<DaumResponse> searchPosts(String keyword, int maxResult, int days) {
        // 검색 시작 날짜 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(days);

        // URI 생성 (정확도순으로 검색)
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("query", keyword)
                .queryParam("size", maxResult)
                .queryParam("sort", "accuracy");  // accuracy(정확도순) 또는 recency(최신순)

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);

        // HTTP 엔티티 생성
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                Map.class
        );

        // 결과 파싱 및 변환
        List<DaumResponse> results = new ArrayList<>();
        Map<String, Object> body = response.getBody();

        if (body != null && body.containsKey("documents")) {
            List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");

            for (Map<String, Object> doc : documents) {
                // HTML 태그와 엔티티 제거
                String cleanTitle = cleanHtml((String) doc.get("title"));
                String cleanContents = cleanHtml((String) doc.get("contents"));

                DaumResponse thread = DaumResponse.builder()
                        .title(cleanTitle)
                        .contents(cleanContents)
                        .url((String) doc.get("url"))
                        .datetime((String) doc.get("datetime"))
                        .build();

                // 날짜 필터링
                LocalDateTime docDate = LocalDateTime.parse(
                        thread.getDatetime(),
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME
                );

                if (docDate.isAfter(startDate)) {
                    results.add(thread);
                }
            }
        }

        // 날짜 기준으로 내림차순 정렬 (최신 게시글 우선)
        results.sort(Comparator.comparing(DaumResponse::getDatetime).reversed());

        // 최대 결과 수 제한 후 반환
        return results.stream()
                .limit(maxResult)
                .collect(Collectors.toList());
    }

    /**
     * HTML 태그와 엔티티를 모두 제거하는 메서드
     */
    private String cleanHtml(String text) {
        if (text == null) return "";
        // HTML 태그 제거
        text = text.replaceAll("<[^>]+>", "");
        // HTML 엔티티 제거
        text = text.replaceAll("&[^;]*;", "");
        return text;
    }
}

