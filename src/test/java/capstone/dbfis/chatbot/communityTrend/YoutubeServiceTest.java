package capstone.dbfis.chatbot.communityTrend;

import capstone.dbfis.chatbot.domain.communityTrend.dto.YoutubeResponse;
import capstone.dbfis.chatbot.domain.communityTrend.service.YoutubeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class YoutubeServiceTest {

    @Autowired
    private YoutubeService youtubeService;

    @Test
    @DisplayName("YouTube 검색 API 테스트")
    void testYoutubeApi() {
        // given
        String keyword = "삼성";

        // when
        List<YoutubeResponse> videos = youtubeService.searchVideos(keyword, 20, 7);

        // then
        assertNotNull(videos);

        // 결과 출력
        System.out.println("\n=== 검색 결과 ===");
        System.out.println("키워드: " + keyword);

        videos.forEach(video -> {
            System.out.println("제목: " + video.getTitle());
            System.out.println("채널: " + video.getChannelTitle());
            System.out.println("설명: " + video.getDescription());
            System.out.println("URL: " + video.getVideoUrl());
            System.out.println("썸네일: " + video.getThumbnailUrl());
            System.out.println("업로드 날짜: " + video.getPublishedAt());
            System.out.println("-------------------");
        });
    }
}

