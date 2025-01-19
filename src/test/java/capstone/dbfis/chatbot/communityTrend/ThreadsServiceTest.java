package capstone.dbfis.chatbot.communityTrend;

import capstone.dbfis.chatbot.domain.communityTrend.dto.ThreadsResponse;
import capstone.dbfis.chatbot.domain.communityTrend.service.ThreadsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ThreadsServiceTest {

    @Autowired
    private ThreadsService threadsService;

    @Test
    void searchThreadsTest() {
        // given
        ThreadsRequest request = new ThreadsRequest(
                "ai",  // 검색어
                10,    // 최대 결과 수
                7      // 최근 7일
        );

        // when
        List<ThreadsResponse> response = threadsService.searchThreads(request);

        // then
        assertNotNull(response);
        System.out.println("=== Threads API 검색 결과 ===");
        if (response.isEmpty()) {
            System.out.println("검색 결과가 없습니다");
        } else {
            response.forEach(thread -> {
                System.out.println("ID: " + thread.getId());
                System.out.println("사용자명: " + thread.getUsername());
                System.out.println("내용: " + thread.getCaption());
                System.out.println("작성일: " + thread.getTimestamp());
                System.out.println("------------------------");
            });
        }

        // API 응답 검증
        assertDoesNotThrow(() -> threadsService.searchThreads(request));
    }
}




