package capstone.dbfis.chatbot.domain.chatbot.controller;

import capstone.dbfis.chatbot.domain.chatbot.dto.QueryRequest;
import capstone.dbfis.chatbot.domain.chatbot.dto.QueryResponse;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatMessage;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.service.ChatMessageService;
import capstone.dbfis.chatbot.domain.chatbot.service.ChatRoomService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotApiController {

    private final WebClient webClient;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final TokenProvider tokenProvider;

    // JWT에서 사용자 ID 추출
    private String extractUserIdFromJWT(String token) {
        return tokenProvider.getMemberId(token);
    }

    @PostMapping("/chatroom")
    @Operation(summary = "새로운 채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    public ChatRoom createChatRoom(@RequestHeader("Authorization") String token) {
        String userId = extractUserIdFromJWT(token);
        return chatRoomService.createChatRoom(userId);
    }

    @DeleteMapping("/chatroom/{chatroomId}")
    @Operation(summary = "채팅방 삭제", description = "해당 채팅방과 메시지를 모두 삭제합니다.")
    public void deleteChatRoom(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatroomId) {

        String userId = extractUserIdFromJWT(token);
        chatRoomService.deleteChatRoomAndMessages(chatroomId, userId);
    }


    @GetMapping("/chatrooms")
    @Operation(summary = "사용자의 모든 채팅방 조회", description = "사용자의 모든 채팅방을 조회합니다.")
    public List<ChatRoom> getUserChatRooms(@RequestHeader("Authorization") String token) {
        String userId = extractUserIdFromJWT(token);
        return chatRoomService.getChatRoomsByUserId(userId);
    }


    @GetMapping("/chatroom/{chatroomId}/messages")
    @Operation(summary = "특정 채팅방의 메시지 조회 (chatroomId 기반)", description = "특정 채팅방의 메시지 리스트를 조회합니다.")
    public List<ChatMessage> getChatMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatroomId) {

        String userId = extractUserIdFromJWT(token);
        ChatRoom chatRoom = chatRoomService.getChatRoomByIdAndUserId(chatroomId, userId);
        return chatMessageService.getMessagesByChatRoom(chatRoom);
    }


    @PostMapping("/chatroom/{chatroomId}/query")
    @Operation(summary = "사용자 입력을 Flask 서버로 전송하고 결과를 받아 채팅 메시지로 저장", description = "사용자 입력을 Flask 서버로 전송하고 결과를 받아 RDB에 저장합니다.")
    public Mono<QueryResponse> handleUserQuery(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatroomId,
            @RequestBody QueryRequest queryRequest) {

        String userId = extractUserIdFromJWT(token);
        ChatRoom chatRoom = chatRoomService.getChatRoomByIdAndUserId(chatroomId, userId);

        return webClient.post()
                .uri("/query")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(queryRequest)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("서버 오류: " + response.statusCode()))))
                .bodyToMono(String.class)
                .flatMap(response -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        return Mono.just(objectMapper.readValue(response, QueryResponse.class));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("JSON 파싱 오류: " + e.getMessage()));
                    }
                })
                .map(response -> {
                    if (response.getResults() != null && response.getResults().getGptResponse() != null) {
                        response.getResults().setGptResponse(
                                new String(response.getResults().getGptResponse().getBytes(), StandardCharsets.UTF_8)
                        );
                    }
                    return response;
                })
                .flatMap(response -> {
                    if (response.getResults() != null && response.getResults().getGptResponse() != null) {
                        chatMessageService.saveMessage(
                                queryRequest.getQuery(),
                                response.getResults().getGptResponse(),
                                userId,
                                chatRoom
                        );
                    }
                    return Mono.just(response);
                });
    }
}