package capstone.dbfis.chatbot.domain.chatbot.controller;

import capstone.dbfis.chatbot.domain.chatbot.dto.QueryRequest;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatMessage;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.service.ChatMessageService;
import capstone.dbfis.chatbot.domain.chatbot.service.ChatRoomService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotApiController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final TokenProvider tokenProvider;

    @PostMapping("/chatroom")
    @Operation(summary = "새로운 채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    public ChatRoom createChatRoom(@RequestHeader("Authorization") String token) {
        String memberId = tokenProvider.getMemberId(token); // JWT에서 사용자 ID 추출
        return chatRoomService.createChatRoom(memberId);
    }

    @DeleteMapping("/chatroom/{chatroomId}")
    @Operation(summary = "채팅방 삭제", description = "해당 채팅방과 메시지를 모두 삭제합니다.")
    public void deleteChatRoom(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatroomId) {

        String memberId = tokenProvider.getMemberId(token); // JWT에서 사용자 ID 추출
        chatRoomService.deleteChatRoomAndMessages(chatroomId, memberId);
    }


    @GetMapping("/chatrooms")
    @Operation(summary = "사용자의 모든 채팅방 조회", description = "사용자의 모든 채팅방을 조회합니다.")
    public List<ChatRoom> getUserChatRooms(@RequestHeader("Authorization") String token) {
        String memberId = tokenProvider.getMemberId(token);
        return chatRoomService.getChatRoomsByMemberId(memberId);
    }

    @GetMapping("/chatroom/{chatroomId}/messages")
    @Operation(summary = "특정 채팅방의 메시지 조회 (chatroomId 기반)", description = "특정 채팅방의 메시지 리스트를 조회합니다.")
    public List<ChatMessage> getChatMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatroomId) {

        String memberId = tokenProvider.getMemberId(token); // JWT에서 사용자 ID 추출
        ChatRoom chatRoom = chatRoomService.getChatRoomByIdAndMemberId(chatroomId, memberId);
        return chatMessageService.getMessagesByChatRoom(chatRoom);
    }

    @PostMapping(value = "/chatroom/{chatroomId}/agent-query", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> handleLangchainStreamQuery(
            @RequestHeader("Authorization") String authToken,
            @PathVariable Long chatroomId,
            @RequestBody QueryRequest queryRequest) {

        String memberId = tokenProvider.getMemberId(authToken);
        ChatRoom chatRoom = chatRoomService.getChatRoomByIdAndMemberId(chatroomId, memberId);

        return chatMessageService.processAgentQuery(queryRequest.getQuery(), memberId, chatRoom);
    }
}