package capstone.dbfis.chatbot.domain.chatbot.controller;

import capstone.dbfis.chatbot.domain.chatbot.entity.ChatMessage;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.service.ChatMessageService;
import capstone.dbfis.chatbot.domain.chatbot.service.ChatRoomService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotApiController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final TokenProvider tokenProvider;
    private final WebClient webClient;

    @Value("${fastapi.url}")
    private String fastapiUrl;

    @PostMapping("/chatroom")
    @Operation(summary = "새로운 채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    public ChatRoom createChatRoom(@RequestHeader("Authorization") String token) {
        String memberId = tokenProvider.getMemberId(token);
        return chatRoomService.createChatRoom(memberId);
    }

    @DeleteMapping("/chatroom/{chatroomId}")
    @Operation(summary = "채팅방 삭제", description = "해당 채팅방과 메시지를 모두 삭제합니다.")
    public void deleteChatRoom(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatroomId) {
        String memberId = tokenProvider.getMemberId(token);
        chatRoomService.deleteChatRoomAndMessages(chatroomId, memberId);
    }

    @GetMapping("/chatrooms")
    @Operation(summary = "사용자의 모든 채팅방 조회", description = "사용자의 모든 채팅방을 조회합니다.")
    public List<ChatRoom> getUserChatRooms(@RequestHeader("Authorization") String token) {
        String memberId = tokenProvider.getMemberId(token);
        return chatRoomService.getChatRoomsByMemberId(memberId);
    }

    @GetMapping("/chatroom/{chatroomId}/messages")
    @Operation(summary = "특정 채팅방의 메시지 조회", description = "특정 채팅방의 메시지 리스트를 조회합니다.")
    public List<ChatMessage> getChatMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatroomId) {
        String memberId = tokenProvider.getMemberId(token);
        ChatRoom chatRoom = chatRoomService.getChatRoomByIdAndMemberId(chatroomId, memberId);
        return chatMessageService.getMessagesByChatRoom(chatRoom);
    }

    @GetMapping(value = "/chatroom/{chatroomId}/agent-query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> proxyStream(@PathVariable Long chatroomId,
                                    @RequestParam String query,
                                    @RequestHeader("Authorization") String authHeader) {

        String memberId = tokenProvider.getMemberId(authHeader);

        return webClient.post()
                .uri(fastapiUrl + "/agent/query")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("Authorization", authHeader)
                .bodyValue(Map.of("query", query, "chat_room_id", chatroomId, "member_id", memberId))
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> !line.isBlank() && !line.equals("data: [DONE]"));
    }

    /**
     * SSE 스트리밍 멀티 에이전트 리서치 호출
     * Client(브라우저) -> Spring Boot (GET) -> FastAPI (POST) -> Spring Boot streams back to client
     */
    @GetMapping(path = "/multi/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> researchMultiStream(
            @RequestParam String topic,
            @RequestParam(defaultValue = "3") int steps,
            @RequestParam(defaultValue = "5") int top_k
    ) {
        Map<String, Object> payload = Map.of(
                "topic", topic,
                "steps", steps,
                "top_k", top_k
        );

        return webClient.post()
                .uri(fastapiUrl + "/research/multi/stream")
                .bodyValue(payload)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .map(data -> ServerSentEvent.builder(data).build())
                .onErrorResume(e -> Flux.just(
                        ServerSentEvent.builder("error: " + e.getMessage()).build()
                ));
    }
}
