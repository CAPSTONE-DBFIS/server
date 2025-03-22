package capstone.dbfis.chatbot.domain.community.service;

import capstone.dbfis.chatbot.domain.community.entity.Comment;
import capstone.dbfis.chatbot.domain.community.entity.Hashtag;
import capstone.dbfis.chatbot.domain.community.entity.Post;
import capstone.dbfis.chatbot.domain.community.repository.BoardRepository;
import capstone.dbfis.chatbot.domain.community.repository.CommentRepository;
import capstone.dbfis.chatbot.domain.community.repository.HashtagRepository;
import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final HashtagRepository hashtagRepository;
    private final CommentRepository commentRepository;

    public BoardService(BoardRepository boardRepository, MemberRepository memberRepository, TokenProvider tokenProvider, CommentRepository commentRepository, HashtagRepository hashtagRepository) {
        this.boardRepository = boardRepository;
        this.tokenProvider = tokenProvider;
        this.memberRepository = memberRepository;
        this.hashtagRepository = hashtagRepository;
        this.commentRepository = commentRepository;
    }

    public List<Post> boardlist() {
        return boardRepository.findAll();
    }

    public String authMember(String authorizationHeader) {

        String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;

        // 토큰에서 Member ID 추출
        String memberId = tokenProvider.getMemberId(token);
        System.out.println("Authenticated Member ID: " + memberId);

        return memberId;
    }

    public void postWrite(Map<String, Object> request, String authorizationHeader) {

        String memberId = authMember(authorizationHeader);
        // Member ID로 Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        String title = (String) request.get("title");
        String content = (String) request.get("content");
        List<String> hashtags = (List<String>) request.getOrDefault("hashtags", new ArrayList<>());


        Post post = new Post(title, content, member, null);

        boardRepository.save(post);

        for (String tagName : hashtags) {
            Hashtag hashtag = hashtagRepository.findByName(tagName)
                    .orElseGet(() -> new Hashtag(tagName));

            hashtag.getPosts().add(post);
            post.setHashtag(hashtag);

            hashtagRepository.save(hashtag);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", "게시글이 작성되었습니다.");
        response.put("postId", post.getId());
        ResponseEntity.ok(response);
    }

    public void deletePost(Long id, String authorizationHeader) {
        Post post = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (!post.getMember().getId().equals(authMember(authorizationHeader))) {
            throw new AccessDeniedException("권한이 없습니다.");
        }
        // 게시글 삭제
        boardRepository.delete(post);
    }

    public void updatePost(Long id, Post post, String authorizationHeader){
        Post postTemp = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (!postTemp.getMember().getId().equals(authMember(authorizationHeader))) {
            throw new AccessDeniedException("권한이 없습니다.");
        }
        // 게시글 수정
        postTemp.setTitle(post.getTitle());
        postTemp.setContent(post.getContent());

        // 게시글 업데이트
        boardRepository.save(postTemp);

    }

    public Map<String, Object> getPostWithComments(Long id) {
        Post post = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        List<Comment> comments = commentRepository.findByPostId(id);

        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("comments", comments);

        return result;
    }
}

