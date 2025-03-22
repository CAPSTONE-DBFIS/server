package capstone.dbfis.chatbot.domain.community.service;

import capstone.dbfis.chatbot.domain.community.entity.Post;
import capstone.dbfis.chatbot.domain.community.entity.PostEmotion;
import capstone.dbfis.chatbot.domain.community.repository.BoardRepository;
import capstone.dbfis.chatbot.domain.community.repository.EmotionRepository;
import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class EmotionService {
    private final EmotionRepository emotionRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public EmotionService(final EmotionRepository emotionRepository, BoardRepository boardRepository, MemberRepository memberRepository, TokenProvider tokenProvider) {
        this.emotionRepository = emotionRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    public String addEmotion(Long postId, String authorizationHeader, Map<String, String> request) {
        String token = authorizationHeader.replace("Bearer ", "");
        String memberId = tokenProvider.getMemberId(token);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        Post post = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Optional<PostEmotion> existingEmotion = emotionRepository.findByPostAndMember(post, member);
        String emotionType = request.get("emotion");

        if (existingEmotion.isPresent() && existingEmotion.get().getEmotion().equals(emotionType)) {
            // 같은 감정을 눌렀다면 삭제
            emotionRepository.delete(existingEmotion.get());
            return "감정 취소됨";
        } else {
            // 새로운 감정 추가
            existingEmotion.ifPresent(emotionRepository::delete);  // 기존 감정 삭제
            emotionRepository.save(new PostEmotion(post, member, emotionType));
            return "감정 등록됨";
        }
    }

    public Optional<PostEmotion> checkEmotionStatus(Long postId, String authorizationHeader){

        String token = authorizationHeader.replace("Bearer ", "");
        String memberId = tokenProvider.getMemberId(token);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        Post post = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        return emotionRepository.findByPostAndMember(post, member);

    }

    public Map<String, Integer> checkEmotionCounts(Long postId){

        Post post = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Map<String, Integer> emotionCounts = new HashMap<>();
        emotionCounts.put("like", emotionRepository.countByPostAndEmotion(post, "like"));
        emotionCounts.put("angry", emotionRepository.countByPostAndEmotion(post, "angry"));
        emotionCounts.put("sad", emotionRepository.countByPostAndEmotion(post, "sad"));
        emotionCounts.put("impressed", emotionRepository.countByPostAndEmotion(post, "impressed"));
        emotionCounts.put("cheer", emotionRepository.countByPostAndEmotion(post, "cheer"));

        return emotionCounts;
    }
}
