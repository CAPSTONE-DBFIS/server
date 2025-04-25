package capstone.dbfis.chatbot.domain.chatbot.controller;

import capstone.dbfis.chatbot.domain.chatbot.dto.ChatDashboardDto;
import capstone.dbfis.chatbot.domain.chatbot.dto.ChatRoomDto;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatMessage;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoomType;
import capstone.dbfis.chatbot.domain.chatbot.service.ChatMessageService;
import capstone.dbfis.chatbot.domain.chatbot.service.ChatRoomService;
import capstone.dbfis.chatbot.domain.team.project.dto.ProjectResponse;
import capstone.dbfis.chatbot.domain.team.project.service.ProjectService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotApiController {
    private final ProjectService projectService;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final TokenProvider tokenProvider;
    private final WebClient webClient;

    @Value("${fastapi.url}")
    private String fastapiUrl;

    @Operation(summary = "챗봇 대시보드", description = "사용자가 속해있는 프로젝트, 프로젝트별 채팅방, 개인 채팅방을 조회합니다.")
    @GetMapping("/dashboard")
    public ChatDashboardDto getDashboard(@RequestHeader("Authorization") String token) {
        String memberId = tokenProvider.getMemberId(token);

        // 사용자가 속해있는 프로젝트 조회
        List<ProjectResponse> projects = projectService.getProjectsByMember(memberId);

        // 사용자가 속해있는 모든 채팅방 조회
        List<ChatRoom> rooms = chatRoomService.getChatRoomsByMemberId(memberId);

        // ChatRoom → ChatRoomDto 변환
        List<ChatRoomDto> roomDtos = rooms.stream()
                .map(r -> ChatRoomDto.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .type(r.getType())
                        .projectId(r.getProjectId())
                        .build())
                .toList();

        // 프로젝트별 채팅방 매핑
        Map<Long, List<ChatRoomDto>> projectChatrooms = roomDtos.stream()
                .filter(dto -> dto.getProjectId() != null)
                .collect(Collectors.groupingBy(ChatRoomDto::getProjectId));

        // 개인용 채팅방 필터링
        List<ChatRoomDto> personalRooms = roomDtos.stream()
                .filter(dto -> dto.getProjectId() == null)
                .collect(Collectors.toList());

        // 최종 DTO 반환
        return ChatDashboardDto.builder()
                .projects(projects)
                .projectChatrooms(projectChatrooms)
                .personalChatrooms(personalRooms)
                .build();
    }

    @PostMapping("/chatroom")
    @Operation(summary = "새로운 채팅방 생성", description = "새로운 채팅방을 생성합니다.")
    public ResponseEntity<ChatRoomDto> createChatRoom(
            @RequestHeader("Authorization") String token,
            @RequestParam ChatRoomType type,
            @RequestParam(required = false) Long projectId) {
        String memberId = tokenProvider.getMemberId(token);
        ChatRoom chatRoom = chatRoomService.createChatRoom(memberId, type, projectId);
        ChatRoomDto chatRoomDto = ChatRoomDto.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .type(chatRoom.getType())
                .projectId(chatRoom.getProjectId())
                .build();
        return ResponseEntity.ok(chatRoomDto);
    }

    @PatchMapping("/chatroom/{chatroomId}/rename")
    @Operation(summary = "채팅방 이름 수정", description = "채팅방의 이름을 수정합니다.")
    public ResponseEntity<String> renameChatRoom(@PathVariable Long chatroomId, @RequestParam String newChatroomName,
                                                 @RequestHeader("Authorization") String token) {
        String memberId = tokenProvider.getMemberId(token);
        chatRoomService.updateChatRoomName(memberId, chatroomId, newChatroomName);
        return ResponseEntity.ok("채팅방 이름이 성공적으로 수정되었습니다.");
    }

    @DeleteMapping("/chatroom/{chatroomId}")
    @Operation(summary = "채팅방 삭제", description = "해당 채팅방과 메시지를 모두 삭제합니다.")
    public void deleteChatRoom(@RequestHeader("Authorization") String token, @PathVariable Long chatroomId) {
        String memberId = tokenProvider.getMemberId(token);
        chatRoomService.deleteChatRoomAndMessages(chatroomId, memberId);
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
}
