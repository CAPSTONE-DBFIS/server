package capstone.dbfis.chatbot.domain.community.service;

import capstone.dbfis.chatbot.domain.community.entity.Comment;
import capstone.dbfis.chatbot.domain.community.entity.Post;
import capstone.dbfis.chatbot.domain.community.repository.BoardRepository;
import capstone.dbfis.chatbot.domain.community.repository.CommentRepository;
import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final TokenProvider tokenProvider;  // 토큰에서 memberId 추출

    public CommentService(CommentRepository commentRepository,
                          MemberRepository memberRepository,
                          BoardRepository boardRepository,
                          TokenProvider tokenProvider) {
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
        this.boardRepository = boardRepository;
        this.tokenProvider = tokenProvider;
    }

    // 댓글 추가
    @Transactional
    public void addComment(String authorizationHeader, Long postId, String content) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        String token = authorizationHeader.substring(7);
        String memberId = tokenProvider.getMemberId(token); // 토큰에서 사용자 ID 추출

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

        Post post = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }

        Comment comment = new Comment(post, member, content);
        commentRepository.save(comment);
    }

    // 댓글 수정
    public void updateComment(Long commentId, String authorizationHeader,String content) {

        String token = authorizationHeader.replace("Bearer ", "");
        String memberId = tokenProvider.getMemberId(token); // 토큰에서 memberId 추출

        System.out.println("댓글/토큰에서 추출한 사용자 ID: " + memberId);  // 로그 추가

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getMember().getId().equals(memberId)) {
            System.out.println("권한 없음 - 작성자 ID: " + comment.getMember().getId());
            throw new IllegalArgumentException("작성자가 일치하지 않습니다.");
        }
        comment.setContent(content);
        commentRepository.save(comment);// 댓글 내용 수정
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, String authorizationHeader) {
        System.out.println("삭제 요청 - 댓글 ID: " + commentId);

        String token = authorizationHeader.replace("Bearer ", "");
        String memberId = tokenProvider.getMemberId(token); // 토큰에서 memberId 추출

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getMember().getId().equals(memberId)) {
            System.out.println("권한 없음 - 작성자 ID: " + comment.getMember().getId());
            throw new IllegalArgumentException("작성자가 일치하지 않습니다.");
        }
        commentRepository.delete(comment);
    }
}
