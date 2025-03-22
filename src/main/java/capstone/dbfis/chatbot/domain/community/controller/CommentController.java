package capstone.dbfis.chatbot.domain.community.controller;

import capstone.dbfis.chatbot.domain.community.entity.Comment;
import capstone.dbfis.chatbot.domain.community.service.CommentService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;


    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 댓글 추가
    @PostMapping("/write")
    @Operation(summary = "댓글 추가", description = "특정 게시글에 댓글을 추가합니다. 요청 헤더에서 JWT 토큰을 통해 사용자를 인증하고, 댓글 내용을 포함한 요청을 처리합니다.")
    public ResponseEntity<Map<String, String>> addComment(@RequestHeader("Authorization") String authorizationHeader,
                                                          @RequestParam Long postId,
                                                          @RequestParam String content) {
        try {
            commentService.addComment(authorizationHeader, postId, content);

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
    @Operation(summary = "댓글 수정", description = "특정 댓글의 내용을 수정합니다. 요청 헤더에서 JWT 토큰을 통해 사용자를 인증하고, 댓글 내용만 수정할 수 있습니다. 작성자만 수정이 가능합니다.")
    public ResponseEntity<String> updateComment(@PathVariable Long id,
                                                @RequestHeader("Authorization") String authorizationHeader,
                                                @RequestBody Map<String, String> request) {

        try{
            commentService.updateComment(id, authorizationHeader,request.get("content"));
            return ResponseEntity.ok("{\"success\": true, \"message\": \"댓글이 수정되었습니다.\"}");

        }
        catch(Exception e) {
            System.out.println("권한 없음 " );
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\": \"작성자만 수정할 수 있습니다.\"}");
        }
    }
    // 댓글 삭제
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다. 삭제 요청자는 댓글 작성자여야 하며, JWT 토큰을 통해 사용자가 인증됩니다.")
    public ResponseEntity<String> deleteComment(@PathVariable Long id,
                                                @RequestHeader("Authorization") String authorizationHeader) {

        try{
            commentService.deleteComment(id, authorizationHeader);
            return ResponseEntity.ok("{\"success\": true, \"message\": \"댓글이 삭제되었습니다.\"}");

        }
        catch(Exception e) {
            System.out.println("권한 없음 " );
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\": \"작성자만 삭제할 수 있습니다.\"}");
        }
    }
}

