package capstone.dbfis.chatbot.domain.chatbot.service;

import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatMessage;
import capstone.dbfis.chatbot.domain.chatbot.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    @Value("${fastapi.agent-url}")
    private String fastAgentUrl;  // FastAPI 기반 LangChain agent 서버 주소

    private final WebClient webClient;  // 비동기 HTTP 통신용 WebClient
    private final ObjectMapper objectMapper;  // JSON 처리용

    /**
     * 채팅 메시지를 비동기적으로 DB에 저장
     */
    @Async
    public void saveMessage(String message, String response, String sender, ChatRoom chatRoom) {
        ChatMessage chatMessage = ChatMessage.builder()
                .message(message)
                .response(response)
                .sender(sender)
                .chatRoom(chatRoom)
                .build();
        chatMessageRepository.save(chatMessage);
    }

    /**
     * LangChain Agent에 쿼리를 전송하고 스트리밍 응답을 Flux<String>으로 반환
     * 최종 응답은 누적하여 비동기적으로 DB에 저장
     */
    public Flux<String> processAgentQuery(String query, String memberId, ChatRoom chatRoom) {
        // 요청 바디 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("chat_room_id", chatRoom.getId());
        requestBody.put("member_id", memberId);

        // 최종 응답 누적용 버퍼
        AtomicReference<StringBuilder> fullResponse = new AtomicReference<>(new StringBuilder());

        return webClient.post()
                .uri(fastAgentUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .map(String::trim)
                .filter(raw -> !raw.equals("data: [DONE]") && !raw.equals("[DONE]"))
                .map(this::extractJson)  // 토큰, 로그, 도구 정보 추출
                .doOnNext(jsonStr -> accumulateToken(jsonStr, fullResponse))  // 토큰 누적
                .doOnComplete(() -> {
                    // 최종 응답 저장
                    String finalResponse = fullResponse.get().toString().trim();
                    System.out.println("[FINAL RESPONSE] " + finalResponse); // 최종 응답 토큰 출력
                    if (!finalResponse.isEmpty()) {
                        saveMessage(query, finalResponse, memberId, chatRoom);
                        System.out.println("[SAVED] 대화 내역 DB 저장 완료");
                    } else {
                        System.err.println("[SKIP] 응답 비어 있음");
                    }
                });
    }

    /**
     * 스트리밍으로 전달된 raw 데이터를 파싱하고 JSON 구조에 따라 필요한 부분만 추출
     */
    private String extractJson(String raw) {
        try {
            String json = raw.startsWith("data:") ? raw.replaceFirst("data: ", "").trim() : raw;
            JsonNode node = objectMapper.readTree(json);

            if (node.has("token")) {
                return objectMapper.writeValueAsString(Map.of("token", node.get("token").asText()));
            } else if (node.has("final")) {
                return objectMapper.writeValueAsString(Map.of("final", node.get("final").asText()));
            } else if (node.has("log")) {
                return objectMapper.writeValueAsString(Map.of("log", node.get("log").asText()));
            } else if (node.has("tool_start")) {
                return objectMapper.writeValueAsString(Map.of(
                        "tool_start", node.get("tool_start").asText(),
                        "input", node.get("input")));
            } else {
                return "";
            }
        } catch (Exception e) {
            System.err.println("[ERROR] JSON 파싱 실패: " + raw + " → " + e.getMessage());
            return "";
        }
    }

    /**
     * 토큰 스트리밍 중 누적 처리 (token만 저장)
     */
    private void accumulateToken(String jsonStr, AtomicReference<StringBuilder> fullResponse) {
        try {
            if (!jsonStr.isEmpty()) {
                JsonNode node = objectMapper.readTree(jsonStr);
                if (node.has("token")) {
                    fullResponse.get().append(node.get("token").asText());
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] 누적 처리 실패: " + jsonStr + " → " + e.getMessage());
        }
    }

    /**
     * 특정 채팅방의 메시지를 시간순으로 조회
     */
    public List<ChatMessage> getMessagesByChatRoom(ChatRoom chatRoom) {
        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
    }

    /**
     * 특정 채팅방에 속한 모든 메시지를 삭제 (트랜잭션 보장)
     */
    @Transactional
    public void deleteMessagesByChatRoom(ChatRoom chatRoom) {
        chatMessageRepository.deleteByChatRoom(chatRoom);
    }
}