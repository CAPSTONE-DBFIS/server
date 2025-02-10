package capstone.dbfis.chatbot.domain.token.controller;

import capstone.dbfis.chatbot.domain.token.service.AccessTokenService;
import capstone.dbfis.chatbot.domain.token.dto.AccessTokenRequest;
import capstone.dbfis.chatbot.domain.token.dto.AccessTokenResponse;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/token")
public class TokenApiController {
    private final AccessTokenService accessTokenService;
    private final TokenProvider tokenProvider;

    @PostMapping()
    @Operation(summary = "리프레쉬 토큰으로 새로운 액세스 토큰 발급", description = "리프레쉬 토큰으로 새로운 액세스 토큰을 발급합니다.")
    public ResponseEntity<AccessTokenResponse> createNewAccessToken(@RequestBody AccessTokenRequest request) {
        // 리프레쉬 토큰으로 새로운 액세스 토큰 발급
        String newAccessToken = accessTokenService.createNewAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
    }

    @GetMapping("/validate")
    @Operation(summary = "JWT 토큰 검증", description = "사용자의 액세스 토큰을 검증합니다.")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7); // "Bearer " 제거
        }

        if (tokenProvider.validateToken(token)) {
            Claims claims = tokenProvider.getClaims(token);
            String userId = claims.get("id", String.class);
            return ResponseEntity.ok().body("유효한 토큰 (사용자 ID: " + userId + ")");
        }
        else {
            return ResponseEntity.status(401).body("유효하지 않은 토큰");
        }
    }
}
