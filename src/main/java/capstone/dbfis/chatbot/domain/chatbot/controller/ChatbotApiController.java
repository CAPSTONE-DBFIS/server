package capstone.dbfis.chatbot.domain.chatbot.controller;

import capstone.dbfis.chatbot.domain.chatbot.dto.AgentQueryResponse;
import capstone.dbfis.chatbot.domain.chatbot.dto.QueryRequest;
import capstone.dbfis.chatbot.domain.chatbot.dto.QueryResponse;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatMessage;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.service.ChatMessageService;
import capstone.dbfis.chatbot.domain.chatbot.service.ChatRoomService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotApiController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final TokenProvider tokenProvider;

    @Value("${flask.server.url}")
    private String flaskUrl; // flask 서버 url

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


    @PostMapping("/chatroom/{chatroomId}/query")
    @Operation(summary = "사용자 입력을 Flask 서버로 전송하고 결과를 받아 반환하고 비동기적으로 DB에 저장",
            description = "사용자 입력을 Flask 서버로 전송하고 결과를 받아 즉시 응답을 반환하고 비동기적으로 RDB에 저장합니다.")
    public ResponseEntity<?> handleUserQuery(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatroomId,
            @RequestBody QueryRequest queryRequest) {

        String memberId = tokenProvider.getMemberId(token); // JWT에서 사용자 ID 추출
        ChatRoom chatRoom = chatRoomService.getChatRoomByIdAndMemberId(chatroomId, memberId);

        // 요청 데이터(JSON) 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", queryRequest.getQuery());
        requestBody.put("chat_room_id", chatroomId);
        requestBody.put("member_id", memberId);

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Flask 서버로 요청 전송 (동기 방식)
        ResponseEntity<QueryResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    flaskUrl,
                    HttpMethod.POST,
                    entity,
                    QueryResponse.class
            );
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Flask 서버 요청 실패: " + e.getMessage());
        }

        // Flask 응답 데이터
        QueryResponse response = responseEntity.getBody();

        // DB 저장을 별도의 스레드에서 비동기적으로 실행
        if (response != null && response.getResults() != null && response.getResults().getGptResponse() != null) {
            chatMessageService.saveMessage(queryRequest.getQuery(), response.getResults().getGptResponse(), memberId, chatRoom);
        }

        return ResponseEntity.ok(response);
    }

    private final String flaskAgentUrl = "http://localhost:5001/agent/query"; // Flask LangChain 서버

    @PostMapping("/chatroom/{chatroomId}/agent-query")
    @Operation(summary = "사용자 입력을 Flask 서버로 전송하고 langchain agent 결과를 받아 반환하고, 비동기적으로 DB에 저장",
            description = "사용자 입력을 Flask 서버로 전송하고 langchain agent 결과를 받아 반환하고, 비동기적으로 DB에 저장합니다.")
    public ResponseEntity<?> handleLangchainQuery(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatroomId,
            @RequestBody QueryRequest queryRequest) {

        String memberId = tokenProvider.getMemberId(token); // JWT에서 사용자 ID 추출
        ChatRoom chatRoom = chatRoomService.getChatRoomByIdAndMemberId(chatroomId, memberId);

        // Flask 요청 데이터 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", queryRequest.getQuery());
        requestBody.put("chat_room_id", chatRoom.getId());
        requestBody.put("member_id", memberId);

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Flask LangChain 서버로 요청 전송 (동기 방식)
        ResponseEntity<AgentQueryResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    flaskAgentUrl,
                    HttpMethod.POST,
                    entity,
                    AgentQueryResponse.class
            );
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Flask LangChain 서버 요청 실패: " + e.getMessage());
        }

        // Flask 응답 데이터
        AgentQueryResponse response = responseEntity.getBody();
        System.out.println("[DEBUG] Flask 응답 원본: " + responseEntity.getBody());

        // DB 저장을 별도의 스레드에서 비동기적으로 실행
        if (response != null && response.getQuery() != null && response.getGptResponse() != null) {
            chatMessageService.saveMessage(queryRequest.getQuery(), response.getGptResponse(), memberId, chatRoom);
        }

        return ResponseEntity.ok(response);
    }


    private final String flaskGraphUrl = "http://localhost:5001/graph/query";
    @PostMapping("/chatroom/{chatroomId}/graph-query")
    @Operation(summary = "사용자 입력을 Flask 서버로 전송하고 langgraph agent 결과를 받아 반환하고, 비동기적으로 DB에 저장",
            description = "사용자 입력을 Flask 서버로 전송하고 langgraph agent 결과를 받아 반환하고, 비동기적으로 DB에 저장합니다.")
    public ResponseEntity<?> handleGraphQuery(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatroomId,
            @RequestBody QueryRequest queryRequest) {

        String memberId = tokenProvider.getMemberId(token); // JWT에서 사용자 ID 추출
        ChatRoom chatRoom = chatRoomService.getChatRoomByIdAndMemberId(chatroomId, memberId);

        // Flask 요청 데이터 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", queryRequest.getQuery());
        requestBody.put("chat_room_id", chatRoom.getId());
        requestBody.put("member_id", memberId);

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Flask LangChain 서버로 요청 전송 (동기 방식)
        ResponseEntity<AgentQueryResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    flaskGraphUrl,
                    HttpMethod.POST,
                    entity,
                    AgentQueryResponse.class
            );
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Flask LangChain 서버 요청 실패: " + e.getMessage());
        }

        // Flask 응답 데이터
        AgentQueryResponse response = responseEntity.getBody();
        System.out.println("[DEBUG] Flask 응답 원본: " + responseEntity.getBody());

        // DB 저장을 별도의 스레드에서 비동기적으로 실행
        if (response != null && response.getQuery() != null && response.getGptResponse() != null) {
            chatMessageService.saveMessage(queryRequest.getQuery(), response.getGptResponse(), memberId, chatRoom);
        }

        return ResponseEntity.ok(response);
    }
}