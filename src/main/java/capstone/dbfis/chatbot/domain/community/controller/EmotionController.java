package capstone.dbfis.chatbot.domain.community.controller;

import capstone.dbfis.chatbot.domain.community.entity.Post;
import capstone.dbfis.chatbot.domain.community.entity.PostEmotion;
import capstone.dbfis.chatbot.domain.community.repository.BoardRepository;
import capstone.dbfis.chatbot.domain.community.repository.EmotionRepository;
import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@Slf4j
@RestController
@RequestMapping("/board")

public class EmotionController {
    private final EmotionRepository emotionRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public EmotionController(final EmotionRepository emotionRepository, BoardRepository boardRepository, MemberRepository memberRepository, TokenProvider tokenProvider) {
        this.emotionRepository = emotionRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/{id}/emotion")
    public ResponseEntity<?> toggleEmotion(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> request) {

        String token = authorizationHeader.replace("Bearer ", "");
        String memberId = tokenProvider.getMemberId(token);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        Post post = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        String emotionType = request.get("emotion");  // "like", "angry", "sad", "happy"

        Optional<PostEmotion> existingEmotion = emotionRepository.findByPostAndMember(post, member);

        if (existingEmotion.isPresent()) {
            log.info("Existing Emotion: {}", existingEmotion.get());
        } else {
            log.info("No existing emotion found for member {} on post {}", memberId, id);
        }

        if (existingEmotion.isPresent() && existingEmotion.get().getEmotion().equals(emotionType)) {
            // 같은 감정을 눌렀다면 삭제
            emotionRepository.delete(existingEmotion.get());
            return ResponseEntity.ok(emotionType + " 취소됨");
        } else {
            // 새로운 감정 추가
            existingEmotion.ifPresent(emotionRepository::delete);  // 기존 감정 삭제
            emotionRepository.save(new PostEmotion(post, member, emotionType));
            return ResponseEntity.ok(emotionType + " 등록됨");
        }
    }

    @GetMapping("/{id}/emotions")
    public ResponseEntity<Map<String, Integer>> getEmotionCounts(@PathVariable Long id) {
        Post post = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Map<String, Integer> emotionCounts = new HashMap<>();
        emotionCounts.put("like", emotionRepository.countByPostAndEmotion(post, "like"));
        emotionCounts.put("angry", emotionRepository.countByPostAndEmotion(post, "angry"));
        emotionCounts.put("sad", emotionRepository.countByPostAndEmotion(post, "sad"));
        emotionCounts.put("impressed", emotionRepository.countByPostAndEmotion(post, "impressed"));
        emotionCounts.put("cheer", emotionRepository.countByPostAndEmotion(post, "cheer"));

        return ResponseEntity.ok(emotionCounts);
    }

    @GetMapping("/{id}/emotion-status")
    public ResponseEntity<Map<String, String>> getEmotionStatus(@PathVariable Long id,
                                                                @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String memberId = tokenProvider.getMemberId(token);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        Post post = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Optional<PostEmotion> postEmotion = emotionRepository.findByPostAndMember(post, member);

        Map<String, String> response = new HashMap<>();
        response.put("emotion", postEmotion.map(PostEmotion::getEmotion).orElse(""));  // 감정 상태 반환
        return ResponseEntity.ok(response);
    }









//    @PostMapping("/{id}/like")
//    public ResponseEntity<?> likePost(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
//        String token = authorizationHeader.replace("Bearer ", "");
//        String memberId = tokenProvider.getMemberId(token);
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));
//
//        Post post = boardRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//
//        if (likeRepository.existsByPostAndMember(post, member)) {
//            return ResponseEntity.badRequest().body("이미 좋아요를 눌렀습니다.");
//        }
//
//        likeRepository.save(new PostLike(post, member));
//        return ResponseEntity.ok("좋아요 성공");
//    }
//
//    @DeleteMapping("/{id}/like")
//    @Transactional
//    public ResponseEntity<?> unlikePost(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
//        String token = authorizationHeader.replace("Bearer ", "");
//        String memberId = tokenProvider.getMemberId(token);
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));
//
//        Post post = boardRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//
//        if (!likeRepository.existsByPostAndMember(post, member)) {
//            return ResponseEntity.badRequest().body("좋아요를 누르지 않았습니다.");
//        }
//
//        likeRepository.deleteByPostAndMember(post, member);
//        return ResponseEntity.ok("좋아요 취소");
//    }
//
//    @GetMapping("/{id}/likes")
//    public ResponseEntity<Integer> getLikeCount(@PathVariable Long id) {
//        Post post = boardRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//        int likeCount = likeRepository.countByPost(post);
//        return ResponseEntity.ok(likeCount);
//    }
//
//    @GetMapping("/{id}/like-status")
//    public ResponseEntity<Boolean> getLikeStatus(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
//        String token = authorizationHeader.replace("Bearer ", "");
//        String memberId = tokenProvider.getMemberId(token);
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));
//
//        Post post = boardRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
//
//        boolean isLiked = likeRepository.existsByPostAndMember(post, member);
//        return ResponseEntity.ok(isLiked);
//    }

}
