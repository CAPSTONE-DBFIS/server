package capstone.dbfis.chatbot.domain.community.controller;

import capstone.dbfis.chatbot.domain.community.entity.Post;
import capstone.dbfis.chatbot.domain.community.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/board/write")
    @Operation(summary = "게시글 작성 폼", description = "사용자가 새로운 게시글을 작성할 수 있는 폼을 반환합니다.")
    public String boardWriteForm(){

        return"boardwrite";
    }

    @PostMapping("/board/writedo")
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성하고, 작성 후 게시글 목록 페이지로 리다이렉트합니다.")
    public String writePost(@RequestBody Map<String, Object> request, @RequestHeader("Authorization") String authorizationHeader) {
        boardService.postWrite(request,authorizationHeader);
        return "redirect:/api/board/list";
    }

    @GetMapping("/board/list")
    @Operation(summary = "게시글 목록", description = "모든 게시글을 조회하여 게시글 목록을 반환합니다.")
    public String boardList(Model model){

        model.addAttribute("list",boardService.boardlist());

        return "boardlist";
    }

    @GetMapping("/board/view/{id}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 ID에 해당하는 게시글과 그에 관련된 댓글들을 조회하여 상세 페이지를 반환합니다.")
    public String boardView(@PathVariable Long id, Model model) {
        Map<String, Object> postWithComments = boardService.getPostWithComments(id);

        model.addAttribute("board", postWithComments.get("post"));
        model.addAttribute("comments", postWithComments.get("comments"));


        return "boardview";
    }

    @PostMapping("/board/delete/{id}")
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제하고, 삭제 후 게시글 목록 페이지로 리다이렉트합니다.")
    public String boardDelete(@PathVariable("id") Long id, @RequestHeader("Authorization") String authorizationHeader) {


    boardService.deletePost(id, authorizationHeader);

    return "redirect:/api/board/list"; // 게시글 목록 페이지로 리다이렉트
    }

    @GetMapping("/board/modify/{id}")
    @Operation(summary = "게시글 수정 폼 반환", description = "게시글 ID에 해당하는 게시글을 수정할 수 있는 폼을 반환합니다.")
    public String boardModify(@PathVariable Long id, Model model) {
        Map<String, Object> postWithComments = boardService.getPostWithComments(id);
        model.addAttribute("post", postWithComments.get("post"));
        return "boardmodify";
    }

    @PostMapping("/board/update/{id}")
    @Operation(summary = "게시글 수정", description = "게시글을 수정하고, 수정 후 게시글 목록 페이지로 리다이렉트합니다.")
    public String boardUpdate(@PathVariable("id") Long id, @RequestBody Post post, @RequestHeader("Authorization") String authorizationHeader) {
        boardService.updatePost(id ,post, authorizationHeader);
        return "redirect:/api/board/list";
    }
}
