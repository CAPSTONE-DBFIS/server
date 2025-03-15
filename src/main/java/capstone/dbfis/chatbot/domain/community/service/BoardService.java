package capstone.dbfis.chatbot.domain.community.service;

import capstone.dbfis.chatbot.domain.community.dto.PostResponseDto;
import capstone.dbfis.chatbot.domain.community.entity.Post;
import capstone.dbfis.chatbot.domain.community.repository.BoardRepository;
import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;
    private MemberRepository memberRepository;
    private TokenProvider tokenProvider;

    public void Write(Post post){

        boardRepository.save(post);
    }

    public List<Post> boardlist(){
        return boardRepository.findAll();
    }

//    public Post boardview(Long id){
//        return boardRepository.findById(id).get();
//    }

    public Post boardview(Long id){
        return boardRepository.findById(id).get();
    }

    public void boardDelete(Long id, HttpServletRequest request){
        String token = extractToken(request);
        String memberIdFromToken = tokenProvider.getMemberId(token);

        Post post = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!post.getMember().equals(memberIdFromToken)) {
            throw new AccessDeniedException("게시글을 삭제할 권한이 없습니다.");
        }

        boardRepository.delete(post);
    }

    private String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // "Bearer " 부분 제거
        }
        throw new IllegalStateException("토큰이 존재하지 않습니다.");
    }
}
