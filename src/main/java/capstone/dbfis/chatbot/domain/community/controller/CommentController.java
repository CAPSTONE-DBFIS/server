package capstone.dbfis.chatbot.domain.community.controller;

import capstone.dbfis.chatbot.domain.community.entity.Comment;
import capstone.dbfis.chatbot.domain.community.service.CommentService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;
    private final TokenProvider tokenProvider;


    public CommentController(CommentService commentService, TokenProvider tokenProvider) {
        this.commentService = commentService;
        this.tokenProvider = tokenProvider;

    }

    // 댓글 추가
    @PostMapping("/write")
    public ResponseEntity<Map<String, String>> addComment(@RequestHeader("Authorization") String authorizationHeader,
                                                          @RequestParam Long postId,
                                                          @RequestParam String content) {
        String token = authorizationHeader.replace("Bearer ", "");

        if (token.isEmpty() || content.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "토큰 또는 댓글 내용이 잘못되었습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            commentService.addComment(token, postId, content);

            Map<String, String> response = new HashMap<>();
            response.put("message", "댓글이 추가되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "댓글 작성 중 문제가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 댓글 수정
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateComment(@PathVariable Long id,
                                                @RequestHeader("Authorization") String authorizationHeader,
                                                @RequestBody Map<String, String> request) {
        System.out.println("수정 요청 - 댓글 ID: " + id);

        String token = authorizationHeader.replace("Bearer ", "");
        String memberId = tokenProvider.getMemberId(token); // 토큰에서 memberId 추출

        System.out.println("토큰에서 추출한 사용자 ID: " + memberId);  // 로그 추가

        Comment comment = commentService.findById(id);

        if (comment == null) {
            System.out.println("해당 ID의 댓글을 찾을 수 없음!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\": \"해당 댓글을 찾을 수 없습니다.\"}");
        }

        if (!comment.getMember().getId().equals(memberId)) {
            System.out.println("권한 없음 - 작성자 ID: " + comment.getMember().getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\": \"작성자만 수정할 수 있습니다.\"}");
        }

        commentService.updateComment(id, request.get("content"));

        return ResponseEntity.ok("{\"success\": true, \"message\": \"댓글이 수정되었습니다.\"}");
    }
    // 댓글 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable Long id,
                                                @RequestHeader("Authorization") String authorizationHeader) {
        System.out.println("삭제 요청 - 댓글 ID: " + id);

        String token = authorizationHeader.replace("Bearer ", "");
        String memberId = tokenProvider.getMemberId(token); // 토큰에서 memberId 추출

        Comment comment = commentService.findById(id);

        if (comment == null) {
            System.out.println("해당 ID의 댓글을 찾을 수 없음!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"message\": \"해당 댓글을 찾을 수 없습니다.\"}");
        }

        if (!comment.getMember().getId().equals(memberId)) {
            System.out.println("권한 없음 - 작성자 ID: " + comment.getMember().getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\": \"작성자만 삭제할 수 있습니다.\"}");
        }

        commentService.deleteComment(id);
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }
}

