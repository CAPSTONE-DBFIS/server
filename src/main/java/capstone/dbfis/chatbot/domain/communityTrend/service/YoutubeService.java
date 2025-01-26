package capstone.dbfis.chatbot.domain.communityTrend.service;

import capstone.dbfis.chatbot.domain.communityTrend.dto.YoutubeResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * YouTube API를 사용하여 동영상을 검색하는 서비스 클래스
 */
@Service
@Slf4j
public class YoutubeService {

    // YouTube API 키
    @Value("${youtube.api.key}")
    private String apiKey;

    /**
     * YouTube 동영상을 검색하는 메서드
     * @param query 검색어
     * @param maxResults 최대 검색 결과 수
     * @param days 검색할 기간(일)
     * @return 검색된 동영상 목록
     */
    public List<YoutubeResponse> searchVideos(String query, int maxResults, int days) {
        try {
            // YouTube API 클라이언트 초기화
            YouTube youtube = new YouTube.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    request -> {})
                    .setApplicationName("youtube-search")
                    .build();
            // 검색 시작 날짜 계산 (현재 시간으로부터 days일 전)
            LocalDateTime dateTime = LocalDateTime.now().minusDays(days);
            String publishedAfter = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

            // 검색 요청 설정
            YouTube.Search.List search = youtube.search().list(Collections.singletonList("snippet"));
            search.setKey(apiKey);
            search.setQ(query);  // 검색어 설정
            search.setType(Collections.singletonList("video"));  // 비디오만 검색
            search.setMaxResults((long) Math.min(maxResults, 50));  // 최대 결과 수 설정
            search.setRegionCode("KR");  // 한국 지역으로 제한
            search.setOrder("relevance");  // 관련도 정렬
            search.setPublishedAfter(publishedAfter);

            // API 호출 및 결과 받기
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();

            // 검색 결과가 없는 경우 처리
            if (searchResultList == null || searchResultList.isEmpty()) {
                log.info("검색 결과가 없습니다. 검색어: {}", query);
                return Collections.emptyList();
            }

            // 검색 결과를 YoutubeResponse 객체로 변환 후 날짜순으로 정렬
            return searchResultList.stream()
                    .map(this::convertToYoutubeResponse)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(YoutubeResponse::getPublishedAt).reversed()) // 최신순 정렬
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("YouTube API 호출 중 오류 발생: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * SearchResult를 YoutubeResponse로 변환하는 메서드
     * @param searchResult YouTube API 검색 결과
     * @return 변환된 YoutubeResponse 객체
     */
    private YoutubeResponse convertToYoutubeResponse(SearchResult searchResult) {
        try {
            ResourceId resourceId = searchResult.getId();
            SearchResultSnippet snippet = searchResult.getSnippet();
            ThumbnailDetails thumbnails = snippet.getThumbnails();

            // YoutubeResponse 객체 생성 및 반환
            return YoutubeResponse.builder()
                    .videoId(resourceId.getVideoId())  // 비디오 ID
                    .title(cleanHtml(snippet.getTitle()))  // 제목
                    .description(cleanHtml(snippet.getDescription()))  // 설명
                    .channelTitle(cleanHtml(snippet.getChannelTitle()))  // 채널명
                    .publishedAt(snippet.getPublishedAt().toStringRfc3339())  // 업로드 날짜
                    .thumbnailUrl(thumbnails.getHigh().getUrl())  // 썸네일 URL
                    .videoUrl("https://www.youtube.com/watch?v=" + resourceId.getVideoId())  // 동영상 URL
                    .build();
        } catch (Exception e) {
            log.error("YouTube 응답 변환 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    /**
     * HTML 태그와 엔티티를 모두 제거하는 메서드
     * @param text HTML 태그와 엔티티가 포함된 문자열
     * @return 정제된 문자열
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
