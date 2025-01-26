package capstone.dbfis.chatbot.domain.communityTrend.service;

import capstone.dbfis.chatbot.domain.communityTrend.dto.RedditPost;
import capstone.dbfis.chatbot.domain.communityTrend.dto.RedditResponse;
import capstone.dbfis.chatbot.domain.communityTrend.dto.RedditTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedditService {

    private final RestTemplate restTemplate;

    @Value("${reddit.client.id}")
    private String clientId;

    @Value("${reddit.client.secret}")
    private String clientSecret;

    @Value("${reddit.username}")
    private String username;

    @Value("${reddit.password}")
    private String password;

    /**
     * Reddit API Access Token 발급
     */
    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", "web:com.dbfis.chatbot:v1.0.0 (by /u/Hot_Mission1860)");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<RedditTokenResponse> response = restTemplate.postForEntity(
                "https://www.reddit.com/api/v1/access_token",
                request,
                RedditTokenResponse.class
        );

        return response.getBody().getAccessToken();
    }

    /**
     * Reddit 키워드 검색 + 날짜 필터링
     *
     * @param keyword 검색할 키워드
     * @param limit   최대 검색 결과 수
     * @param days    최근 N일 이내 게시글만 검색
     * @return 필터링 및 정렬된 Reddit 게시글 리스트
     */
    public List<RedditPost> searchPosts(String keyword, int limit, int days) {
        // Reddit Access Token 발급
        String accessToken = getAccessToken();

        // Reddit API 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);  // OAuth 토큰 추가
        headers.set("User-Agent", "web:com.dbfis.chatbot:v1.0.0 (by /u/Hot_Mission1860)");

        // Reddit 검색 URL 생성
        String url = UriComponentsBuilder
                .fromHttpUrl("https://oauth.reddit.com/search")  // Reddit 검색 API
                .queryParam("q", keyword)       // 검색 키워드
                .queryParam("limit", limit)     // 최대 결과 수
                .queryParam("sort", "relevance")      // 관련도순 정렬
                .queryParam("restrict_sr", "false")  // 서브레딧 제한 X
                .build()
                .toUriString();

        // API 요청 실행
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<RedditResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                RedditResponse.class
        );

        // API 응답에서 게시글 추출
        List<RedditPost> posts = response.getBody().getData().getChildren()
                .stream()
                .map(RedditResponse.RedditChild::getData)
                .toList();

        // 날짜 기준 필터링 및 정렬
        long currentTime = System.currentTimeMillis() / 1000L;  // 현재 시간 (UTC, 초 단위)
        long startTime = currentTime - (days * 24 * 60 * 60);   // N일 전 시간 (UTC)

        List<RedditPost> filteredPosts = posts.stream()
                .filter(post -> post.getCreated_utc() >= startTime) // 최근 N일 게시글 필터링
                .sorted(Comparator.comparing(RedditPost::getCreated_utc).reversed()) // 최신순 정렬
                .limit(limit) // 최대 결과 수 제한
                .toList();

        return filteredPosts;  // 최종 결과 반환
    }
}
