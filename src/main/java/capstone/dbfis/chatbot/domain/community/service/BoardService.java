package capstone.dbfis.chatbot.domain.community.service;

import capstone.dbfis.chatbot.domain.community.entity.Post;
import capstone.dbfis.chatbot.domain.community.repository.BoardRepository;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;
    private MemberRepository memberRepository;
    private TokenProvider tokenProvider;


    public List<Post> boardlist(){
        return boardRepository.findAll();
    }

    public Post boardview(Long id){
        return boardRepository.findById(id).get();
    }

}
