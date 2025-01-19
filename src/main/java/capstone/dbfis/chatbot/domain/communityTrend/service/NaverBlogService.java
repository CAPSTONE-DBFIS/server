package capstone.dbfis.chatbot.domain.communityTrend.service;

import capstone.dbfis.chatbot.domain.communityTrend.dto.NaverBlogResponse;
import capstone.dbfis.chatbot.domain.communityTrend.util.NaverClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 네이버 블로그 검색 API를 활용한 서비스 클래스
 */
@Service
public class NaverBlogService {
    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    @Value("${naver.api.url}")
    private String apiUrl;

    private final NaverClientUtil naverClientUtil;

    public NaverBlogService(NaverClientUtil naverClientUtil) {
        this.naverClientUtil = naverClientUtil;
    }

    /**
     * 네이버 블로그에서 키워드로 게시글을 검색하는 메서드
     *
     * @param keyword 검색할 키워드
     * @param maxResult 최대 검색 결과 갯수
     * @param days 검색할 기간(일 단위)
     * @return 검색된 블로그 게시글 목록
     * @throws RuntimeException API 호출 실패 또는 데이터 파싱 실패 시
     */
    public List<NaverBlogResponse> searchPosts(String keyword, int maxResult, int days) {
        List<NaverBlogResponse> posts = new ArrayList<>();
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        try {
            // maxResult 값을 display 파라미터에 적용 (최대 100개로 제한)
            int display = Math.min(maxResult, 100);  // 네이버 API 최대 제한: 100
            String url = apiUrl + "?query=" + keyword + "&display=" + display;

            String response = naverClientUtil.get(url, clientId, clientSecret);
            JSONObject json = new JSONObject(response);
            JSONArray items = json.getJSONArray("items");

            for (int i = 0; i < Math.min(items.length(), maxResult); i++) {  // maxResult 만큼만 처리
                JSONObject item = items.getJSONObject(i);
                String postDateStr = item.getString("postdate");
                LocalDate postDate = LocalDate.parse(postDateStr, formatter);

                if (!postDate.isBefore(cutoffDate)) {
                    posts.add(NaverBlogResponse.builder()
                            .title(cleanHtml(item.getString("title")))
                            .link(item.getString("link"))
                            .description(cleanHtml(item.getString("description")))
                            .bloggerName(item.getString("bloggername"))
                            .postDate(postDateStr)
                            .build());
                }
            }

            posts.sort(Comparator.comparing(NaverBlogResponse::getPostDate).reversed());

        } catch (Exception e) {
            throw new RuntimeException("네이버 블로그 검색 중 오류 발생: " + e.getMessage());
        }

        // 최종 결과 개수를 maxResult로 제한
        return posts.stream()
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