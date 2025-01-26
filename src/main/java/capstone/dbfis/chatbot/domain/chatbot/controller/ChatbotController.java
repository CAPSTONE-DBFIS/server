package capstone.dbfis.chatbot.domain.chatbot.controller;

import capstone.dbfis.chatbot.domain.chatbot.dto.QueryRequest;
import capstone.dbfis.chatbot.domain.chatbot.dto.QueryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final WebClient webClient;

    public ChatbotController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://127.0.0.1:5001").build();
    }

    @PostMapping("/query")
    public Mono<QueryResponse> handleUserQuery(@RequestBody QueryRequest queryRequest) {
        return webClient.post()
                .uri("/query")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(queryRequest)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        Mono.error(new RuntimeException("잘못된 요청: " + response.statusCode()))
                )
                .onStatus(status -> status.is5xxServerError(), response ->
                        Mono.error(new RuntimeException("서버 오류: " + response.statusCode()))
                )
                .bodyToMono(String.class)  // 먼저 String으로 응답 받기
                .flatMap(response -> {
                    // 응답이 정상일 경우 QueryResponse로 변환
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        QueryResponse queryResponse = objectMapper.readValue(response, QueryResponse.class);
                        return Mono.just(queryResponse);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("JSON 파싱 오류: " + e.getMessage()));
                    }
                })
                .map(response -> {
                    if (response.getResults() != null && response.getResults().getGptResponse() != null) {
                        // 필요한 경우 UTF-8 인코딩 강제 적용
                        response.getResults().setGptResponse(new String(response.getResults().getGptResponse().getBytes(), StandardCharsets.UTF_8));
                    }
                    return response;
                })
                .doOnError(e -> System.err.println("Flask 서버 호출 실패: " + e.getMessage()));
    }
}