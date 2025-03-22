package capstone.dbfis.chatbot.domain.community.controller;

import capstone.dbfis.chatbot.domain.community.entity.PostEmotion;
import capstone.dbfis.chatbot.domain.community.repository.EmotionRepository;
import capstone.dbfis.chatbot.domain.community.service.EmotionService;
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
    public ResponseEntity<String> toggleEmotion(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> request) {

        String result = emotionService.addEmotion(id, authorizationHeader, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/emotions")
    public ResponseEntity<Map<String, Integer>> getEmotionCounts(@PathVariable Long id) {

        Map<String, Integer>counts = emotionService.checkEmotionCounts(id);
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/{id}/emotion-status")
    public ResponseEntity<Map<String, String>> getEmotionStatus(@PathVariable Long id,
                                                                @RequestHeader("Authorization") String authorizationHeader) {
        Map<String, String> response = new HashMap<>();
        response.put("emotion", emotionService.checkEmotionStatus(id,authorizationHeader).map(PostEmotion::getEmotion).orElse(""));  // 감정 상태 반환
        return ResponseEntity.ok(response);
    }

}
