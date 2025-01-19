package capstone.dbfis.chatbot.domain.communityTrend.controller;

import capstone.dbfis.chatbot.domain.communityTrend.dto.*;
import capstone.dbfis.chatbot.domain.communityTrend.service.*;
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
    public ResponseEntity<List<NaverBlogResponse>> searchNaverBlog(@RequestBody SearchRequest request) {
        List<NaverBlogResponse> posts = naverBlogService.searchPosts(request.getKeyword(), request.getMaxResults(), request.getDays());
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/api/youtube")
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
    public ResponseEntity<List<DaumResponse>> searchDaumBlog(@RequestBody SearchRequest request) {
        List<DaumResponse> posts = daumBlogService.searchPosts(request.getKeyword(), request.getMaxResults(), request.getDays());
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/api/reddit")
    public ResponseEntity<List<RedditPost>> searchReddit(@RequestBody SearchRequest request) {
        List<RedditPost> posts = redditService.searchPosts(request.getKeyword(), request.getMaxResults(), request.getDays());
        return ResponseEntity.ok(posts);
    }
}
