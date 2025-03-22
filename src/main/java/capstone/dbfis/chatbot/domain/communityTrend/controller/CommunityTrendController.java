package capstone.dbfis.chatbot.domain.communityTrend.controller;

import capstone.dbfis.chatbot.domain.communityTrend.dto.*;
import capstone.dbfis.chatbot.domain.communityTrend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommunityTrendController {
    private final NaverBlogService naverBlogService;
    private final YoutubeService youtubeService;
//    private final ThreadsService threadsService;
    private final DaumBlogService daumBlogService;
    private final RedditService redditService;

    @PostMapping("/api/naver-blog")
    @Operation(summary = "네이버 블로그 키워드 검색", description = "네이버 블로그 API를 이용해 사용자 입력 키워드를 검색하고 결과를 json 포맷으로 리턴합니다.")
    public ResponseEntity<List<NaverBlogResponse>> searchNaverBlog(@RequestBody SearchRequest request) {
        List<NaverBlogResponse> posts = naverBlogService.searchPosts(request.getKeyword(), request.getMaxResults(), request.getDays());
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/api/youtube")
    @Operation(summary = "유튜브 키워드 검색", description = "유튜브 API를 이용해 사용자 입력 키워드를 검색하고 결과를 json 포맷으로 리턴합니다.")
    public ResponseEntity<List<YoutubeResponse>> searchYouTubeVideos(@RequestBody SearchRequest request) {
        List<YoutubeResponse> videos = youtubeService.searchVideos(request.getKeyword(), request.getMaxResults(), request.getDays());
        return ResponseEntity.ok(videos);
    }

//    @GetMapping("/api/threads")
//    public ResponseEntity<List<ThreadsPost>> searchKeyword(@RequestParam String keyword) {
//        List<ThreadsPost> searchResults = threadsService.searchKeyword(keyword);
//        return ResponseEntity.ok(searchResults);
//    }

    @PostMapping("/api/daum-blog")
    @Operation(summary = "다음 블로그 키워드 검색", description = "kakao API를 이용해 사용자 입력 키워드를 검색하고 결과를 json 포맷으로 리턴합니다.")
    public ResponseEntity<List<DaumResponse>> searchDaumBlog(@RequestBody SearchRequest request) {
        List<DaumResponse> posts = daumBlogService.searchPosts(request.getKeyword(), request.getMaxResults(), request.getDays());
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/api/reddit")
    @Operation(summary = "레딧 키워드 검색", description = "reddit API를 이용해 사용자 입력 키워드를 검색하고 결과를 json 포맷으로 리턴합니다.")
    public ResponseEntity<List<RedditPost>> searchReddit(@RequestBody SearchRequest request) {
        List<RedditPost> posts = redditService.searchPosts(request.getKeyword(), request.getMaxResults(), request.getDays());
        return ResponseEntity.ok(posts);
    }
}
