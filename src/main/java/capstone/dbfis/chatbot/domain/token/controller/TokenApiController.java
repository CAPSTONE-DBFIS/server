package capstone.dbfis.chatbot.domain.token.controller;

import capstone.dbfis.chatbot.domain.token.service.AccessTokenService;
import capstone.dbfis.chatbot.domain.token.dto.CreateAccessTokenRequest;
import capstone.dbfis.chatbot.domain.token.dto.CreateAccessTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TokenApiController {
    private final AccessTokenService accessTokenService;

    @PostMapping("/api/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken
            (@RequestBody CreateAccessTokenRequest request) {

        // 요청으로 받은 리프레시 토큰을 사용하여 새로운 액세스 토큰 생성
        String newAccessToken = accessTokenService
                .createdNewAccessToken(request.getRefreshToken());

        // HTTP 201 CREATED 상태와 함께 새로 생성된 액세스 토큰을 응답 본문으로 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(newAccessToken));
    }
}
