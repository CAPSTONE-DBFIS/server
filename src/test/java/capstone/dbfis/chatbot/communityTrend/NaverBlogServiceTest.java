package capstone.dbfis.chatbot.communityTrend;

import capstone.dbfis.chatbot.domain.communityTrend.dto.NaverBlogResponse;
import capstone.dbfis.chatbot.domain.communityTrend.service.NaverBlogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class NaverBlogServiceTest {

    @Autowired
    private NaverBlogService naverBlogService;

    @Test
    @DisplayName("네이버 블로그 API 테스트")
    void testNaverBlogApi() {
        // 키워드로 API 호출
        String keyword = "AI";
        List<NaverBlogResponse> posts = naverBlogService.searchPosts(keyword, 100, 7);

        // 결과 검증
        assertNotNull(posts, "결과가 null임.");
        assertFalse(posts.isEmpty(), "결과 리스트가 비어있음.");

        // API 호출 결과 확인
        posts.forEach(post -> {
            System.out.println("Title: " + post.getTitle());
            System.out.println("Link: " + post.getLink());
            System.out.println("Description: " + post.getDescription());
            System.out.println("Blogger Name: " + post.getBloggerName());
            System.out.println("Post Date: " + post.getPostDate());
            System.out.println("---------------");
        });
    }
}