package capstone.dbfis.chatbot.domain.chatbot.controller;

import capstone.dbfis.chatbot.domain.chatbot.dto.ChatDashboardDto;
import capstone.dbfis.chatbot.domain.chatbot.dto.ChatRoomDto;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatMessage;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoomType;
import capstone.dbfis.chatbot.domain.chatbot.service.ChatMessageService;
import capstone.dbfis.chatbot.domain.chatbot.service.ChatRoomService;

import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Chatbot API", description = "챗봇 API")
@RequiredArgsConstructor
public class ChatbotApiController {
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final TokenProvider tokenProvider;
    private final WebClient webClient;

    @Value("${fastapi.url}")
    private String fastapiUrl;

    @Operation(summary = "챗봇 대시보드", description = "사용자가 속해있는 팀, 팀 별 채팅방, 개인 채팅방을 조회합니다.")
    @GetMapping("/dashboard")
    public ResponseEntity<ChatDashboardDto> getDashboard(
            @RequestHeader("Authorization") @NotBlank String token) {
        String memberId = tokenProvider.getMemberId(token);

        ChatDashboardDto dto = chatRoomService.buildChatDashboard(memberId);

        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "새로운 채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    @PostMapping("/chatroom")
    public ResponseEntity<ChatRoomDto> createChatRoom(
            @RequestHeader("Authorization") @NotBlank String token,
            @RequestParam ChatRoomType type,
            @RequestParam Long teamId) {

        String memberId = tokenProvider.getMemberId(token);
        ChatRoomDto dto = chatRoomService.createChatRoomAndReturnDto(memberId, type, teamId);

        return ResponseEntity.status(201).body(dto); // 201 Created
    }

    @Operation(summary = "채팅방 이름 수정", description = "채팅방의 이름을 수정합니다.")
    @PatchMapping("/chatroom/{chatroomId}/rename")
    public ResponseEntity<Void> renameChatRoom(
            @PathVariable @Min(1) Long chatroomId,
            @RequestParam @NotBlank String newChatroomName,
            @RequestHeader("Authorization") @NotBlank String token) {

        String memberId = tokenProvider.getMemberId(token);
        chatRoomService.updateChatRoomName(memberId, chatroomId, newChatroomName);
        return ResponseEntity.noContent().build();   // 204 No Content
    }

    @Operation(summary = "채팅방 삭제", description = "해당 채팅방과 메시지를 모두 삭제합니다.")
    @DeleteMapping("/chatroom/{chatroomId}")
    public ResponseEntity<Void> deleteChatRoom(
            @PathVariable @Min(1) Long chatroomId,
            @RequestHeader("Authorization") @NotBlank String token) {

        String memberId = tokenProvider.getMemberId(token);
        chatRoomService.deleteChatRoomAndMessages(chatroomId, memberId);
        return ResponseEntity.noContent().build();   // 204 No Content
    }

    @Operation(summary = "특정 채팅방의 메시지 조회", description = "특정 채팅방의 메시지 리스트를 조회합니다.")
    @GetMapping("/chatroom/{chatroomId}/messages")
    public ResponseEntity<List<ChatMessage>> getChatMessages(
            @PathVariable @Min(1) Long chatroomId,
            @RequestHeader("Authorization") @NotBlank String token) {

        String memberId = tokenProvider.getMemberId(token);
        ChatRoom room = chatRoomService.getChatRoomByIdAndMemberId(chatroomId, memberId);
        List<ChatMessage> messages = chatMessageService.getMessagesByChatRoom(room);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "채팅방 즐겨찾기 설정", description = "채팅방을 즐겨찾기로 등록하거나 즐겨찾기를 해제합니다.")
    @PatchMapping("/chatroom/{chatroomId}/favorite")
    public ResponseEntity<Void> toggleFavoriteChatRoom(
            @PathVariable @Min(1) Long chatroomId,
            @RequestParam boolean favorite,
            @RequestHeader("Authorization") @NotBlank String token) {

        String memberId = tokenProvider.getMemberId(token);
        chatRoomService.updateFavoriteStatus(memberId, chatroomId, favorite);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "에이전트 스트리밍 쿼리 프록시", description = "선택적으로 FastApi에 파일을 업로드 하고, FastAPI SSE 스트림을 프록시합니다.")
    @PostMapping(value = "/chatroom/{chatroomId}/agent-query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> proxyStream(
            @PathVariable @Min(1) Long chatroomId,
            @RequestParam @NotBlank String query,
            @RequestParam(required = false) Long personaId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "llmModelType", required = false) String modelType,
            @RequestHeader("Authorization") @NotBlank String token) throws IOException {

        String memberId = tokenProvider.getMemberId(token);

        MultiValueMap<String, Object> body = chatRoomService.prepareMultipartRequest(query, chatroomId, memberId, personaId, files, modelType);

        return webClient.post()
                .uri(fastapiUrl + "/agent/query")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("Authorization", token)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .map(msg -> new ResponseStatusException(resp.statusCode(), msg))
                )
                .bodyToFlux(String.class)
                .filter(line -> !line.isBlank() && !line.equals("data: [DONE]"));
    }
}
