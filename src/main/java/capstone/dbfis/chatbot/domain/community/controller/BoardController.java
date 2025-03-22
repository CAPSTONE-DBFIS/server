package capstone.dbfis.chatbot.domain.community.controller;

import capstone.dbfis.chatbot.domain.community.entity.Comment;
import capstone.dbfis.chatbot.domain.community.entity.Hashtag;
import capstone.dbfis.chatbot.domain.community.entity.Post;
import capstone.dbfis.chatbot.domain.community.repository.BoardRepository;
import capstone.dbfis.chatbot.domain.community.repository.CommentRepository;
import capstone.dbfis.chatbot.domain.community.repository.HashtagRepository;
import capstone.dbfis.chatbot.domain.community.service.BoardService;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/board/write")
    public String boardWriteForm(){
        return"boardwrite";
    }

    @PostMapping("/board/writedo")
    public String writePost(@RequestBody Map<String, Object> request, @RequestHeader("Authorization") String authorizationHeader) {
        boardService.postWrite(request,authorizationHeader);
        return "redirect:/board/list";
    }

    @GetMapping("/board/list")
    public String boardList(Model model){

        model.addAttribute("list",boardService.boardlist());

        return "boardlist";
    }

    @GetMapping("/board/view/{id}")
    public String boardView(@PathVariable Long id, Model model) {
        Map<String, Object> postWithComments = boardService.getPostWithComments(id);

        model.addAttribute("board", postWithComments.get("post"));
        model.addAttribute("comments", postWithComments.get("comments"));

        return "boardview";
    }

    @PostMapping("/board/delete/{id}")
    public String boardDelete(@PathVariable("id") Long id, @RequestHeader("Authorization") String authorizationHeader) {

    boardService.deletePost(id, authorizationHeader);

    return "redirect:/board/list"; // 게시글 목록 페이지로 리다이렉트
    }

    @GetMapping("/board/modify/{id}")
    public String boardModify(@PathVariable Long id, Model model) {
        Map<String, Object> postWithComments = boardService.getPostWithComments(id);
        model.addAttribute("post", postWithComments.get("post"));
        return "boardmodify";
    }

    @PostMapping("/board/update/{id}")
    public String boardUpdate(@PathVariable("id") Long id, @RequestBody Post post, @RequestHeader("Authorization") String authorizationHeader) {
        boardService.updatePost(id ,post, authorizationHeader);
        return "redirect:/board/list";
    }
}
