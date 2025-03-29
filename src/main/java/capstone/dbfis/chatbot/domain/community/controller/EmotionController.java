package capstone.dbfis.chatbot.domain.community.controller;

import capstone.dbfis.chatbot.domain.community.entity.PostEmotion;
import capstone.dbfis.chatbot.domain.community.repository.EmotionRepository;
import capstone.dbfis.chatbot.domain.community.service.EmotionService;
import io.swagger.v3.oas.annotations.Operation;
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
    private final EmotionService emotionService;

    public EmotionController(final EmotionService emotionService) {
        this.emotionService = emotionService;
    }

    @PostMapping("/{id}/emotion")
    @Operation(summary = "감정 토글", description = "특정 게시글에 대한 감정을 추가하거나 토글합니다. 요청 헤더에서 JWT 토큰을 통해 사용자를 인증하고, 감정 상태를 변경합니다.")
    public ResponseEntity<String> toggleEmotion(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> request) {

        String result = emotionService.addEmotion(id, authorizationHeader, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/emotions")
    @Operation(summary = "감정 상태 카운트", description = "특정 게시글에 대한 감정 상태를 조회하여 각 감정의 수를 반환합니다.")
    public ResponseEntity<Map<String, Integer>> getEmotionCounts(@PathVariable Long id) {

        Map<String, Integer>counts = emotionService.checkEmotionCounts(id);
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/{id}/emotion-status")
    @Operation(summary = "감정 상태 조회", description = "특정 게시글에 대한 사용자의 감정 상태를 조회합니다. 요청 헤더에서 JWT 토큰을 통해 사용자를 인증하고, 해당 사용자의 감정 상태를 반환합니다.")
    public ResponseEntity<Map<String, String>> getEmotionStatus(@PathVariable Long id,
                                                                @RequestHeader("Authorization") String authorizationHeader) {
        Map<String, String> response = new HashMap<>();
        response.put("emotion", emotionService.checkEmotionStatus(id,authorizationHeader).map(PostEmotion::getEmotion).orElse(""));  // 감정 상태 반환
        return ResponseEntity.ok(response);
    }

}
