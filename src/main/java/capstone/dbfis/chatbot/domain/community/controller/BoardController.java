package capstone.dbfis.chatbot.domain.community.controller;

import capstone.dbfis.chatbot.domain.community.entity.Comment;
import capstone.dbfis.chatbot.domain.community.entity.Hashtag;
import capstone.dbfis.chatbot.domain.community.entity.Post;
import capstone.dbfis.chatbot.domain.community.repository.BoardRepository;
import capstone.dbfis.chatbot.domain.community.repository.CommentRepository;
import capstone.dbfis.chatbot.domain.community.repository.HashtagRepository;
import capstone.dbfis.chatbot.domain.community.service.BoardService;
import capstone.dbfis.chatbot.domain.member.entity.Member;
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
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final CommentRepository commentRepository;
    private final HashtagRepository hashtagRepository;

    public BoardController(BoardService boardService, BoardRepository boardRepository, MemberRepository memberRepository, TokenProvider tokenProvider, CommentRepository commentRepository, HashtagRepository hashtagRepository) {
        this.boardService = boardService;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
        this.commentRepository = commentRepository;
        this.hashtagRepository = hashtagRepository;
    }

    @GetMapping("/board/write")
    public String boardWriteForm(){
        return"boardwrite";
    }

    @PostMapping("/board/writedo")
    public ResponseEntity<Map<String, Object>> writePost(@RequestBody Map<String, Object> request,
                                                         @RequestHeader("Authorization") String authorizationHeader) {
<<<<<<< HEAD

        // Member 조회
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        String memberId = tokenProvider.getMemberId(token);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

=======
        // Authorization 헤더에서 토큰 추출
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

        // 토큰에서 Member ID 추출
        String memberId = tokenProvider.getMemberId(token);
        System.out.println("Authenticated Member ID: " + memberId);

        // Member ID로 Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        System.out.println("Member retrieved: " + member);
>>>>>>> e5e3669776904733c2b1ebb8b916513f36c41bc7

        // 요청 데이터 추출
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        List<String> hashtags = (List<String>) request.getOrDefault("hashtags", new ArrayList<>());

<<<<<<< HEAD

        Post post = new Post(title, content, member, null);

=======
        System.out.println("Post Title: " + title);
        System.out.println("Post Content: " + content);
        System.out.println("Extracted Hashtags: " + hashtags);

        // 게시글 객체 생성
        Post post = new Post(title, content, member, null);

        // 게시글 저장
>>>>>>> e5e3669776904733c2b1ebb8b916513f36c41bc7
        boardRepository.save(post);

        // 해시태그 처리
        for (String tagName : hashtags) {
            Hashtag hashtag = hashtagRepository.findByName(tagName)
                    .orElseGet(() -> new Hashtag(tagName));

<<<<<<< HEAD
=======
            // 양방향 관계 설정
>>>>>>> e5e3669776904733c2b1ebb8b916513f36c41bc7
            hashtag.getPosts().add(post);
            post.setHashtag(hashtag);

            hashtagRepository.save(hashtag); // 해시태그 저장
        }

<<<<<<< HEAD
=======
        // JSON 응답 반환
>>>>>>> e5e3669776904733c2b1ebb8b916513f36c41bc7
        Map<String, Object> response = new HashMap<>();
        response.put("message", "게시글이 작성되었습니다.");
        response.put("postId", post.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/board/list")
    public String boardList(Model model){

        model.addAttribute("list",boardService.boardlist());

        return "boardlist";
    }

    @GetMapping("/board/view/{id}")
    public String boardView(@PathVariable Long id, Model model){
        model.addAttribute("board", boardService.boardview(id));
        List<Comment> comments = commentRepository.findByPostId(id);
        model.addAttribute("comments", comments);
        return "boardview";
    }

    @PostMapping("/board/delete/{id}")
    public String boardDelete(@PathVariable("id") Long id, @RequestHeader("Authorization") String authorizationHeader) {
<<<<<<< HEAD

    String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
    String memberId = tokenProvider.getMemberId(token); // TokenProvider의 getMemberId 호출
    Post post = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));

=======
    // Authorization 헤더에서 토큰 추출
    String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

    // 토큰에서 Member ID 추출
    String memberId = tokenProvider.getMemberId(token); // TokenProvider의 getMemberId 호출

    // 게시글 조회
    Post post = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));

    // Member ID로 Member 조회
    Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

>>>>>>> e5e3669776904733c2b1ebb8b916513f36c41bc7
    // 작성자가 로그인한 사용자와 일치하는지 확인
    if (!post.getMember().getId().equals(memberId)) {
        throw new AccessDeniedException("권한이 없습니다.");
    }

    // 게시글 삭제
    boardRepository.delete(post);

    return "redirect:/board/list"; // 게시글 목록 페이지로 리다이렉트
    }

    @GetMapping("/board/modify/{id}")
    public String boardModify(@PathVariable Long id, Model model) {
        // 게시글 조회
        Post post = boardService.boardview(id);

        // Model에 게시글을 추가
        model.addAttribute("post", post);

        // 수정 페이지로 이동
        return "boardmodify";
    }

    @PostMapping("/board/update/{id}")
    public String boardUpdate(@PathVariable("id") Long id, @RequestBody Post post, @RequestHeader("Authorization") String authorizationHeader) {
<<<<<<< HEAD
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        System.out.println("Received update request for postId: " + id);
        String memberId = tokenProvider.getMemberId(token);  // TokenProvider의 getMemberId 호출

        Post postTemp = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));

=======
        // Authorization 헤더에서 토큰 추출
        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
        System.out.println("Received update request for postId: " + id);
        // 토큰에서 Member ID 추출
        String memberId = tokenProvider.getMemberId(token);  // TokenProvider의 getMemberId 호출

        System.out.println("Authenticated Member ID: " + memberId);

        // 게시글 조회
        Post postTemp = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // Member ID로 Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        System.out.println("Member retrieved: " + member);
        System.out.println("Post Title: " + post.getTitle());  // 게시글 제목 출력
        System.out.println("Post Content: " + post.getContent());  // 게시글 내용 출력
        System.out.println("Post Member: " + postTemp.getMember());

        // 작성자가 로그인한 사용자와 일치하는지 확인
>>>>>>> e5e3669776904733c2b1ebb8b916513f36c41bc7
        if (!postTemp.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        // 게시글 수정
        postTemp.setTitle(post.getTitle());
        postTemp.setContent(post.getContent());

        // 게시글 업데이트
        boardRepository.save(postTemp);

<<<<<<< HEAD
=======
        // 게시글 목록 페이지로 리다이렉트
>>>>>>> e5e3669776904733c2b1ebb8b916513f36c41bc7
        return "redirect:/board/list";
    }
}
