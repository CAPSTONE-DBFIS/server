package capstone.dbfis.chatbot.domain.token.controller;

import capstone.dbfis.chatbot.domain.token.service.AccessTokenService;
import capstone.dbfis.chatbot.domain.token.dto.AccessTokenRequest;
import capstone.dbfis.chatbot.domain.token.dto.AccessTokenResponse;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "JWT Token API", description = "JWT 토큰 발급 API")
@RequestMapping("/api/token")
public class TokenApiController {

    private final AccessTokenService accessTokenService;
    private final TokenProvider tokenProvider;

    @Operation(summary = "리프레시 토큰으로 새로운 액세스 토큰 발급",
            description = "리프레시 토큰을 검증하고 새로운 액세스 토큰을 발급합니다.")
    @PostMapping
    public ResponseEntity<AccessTokenResponse> createNewAccessToken(
            @RequestBody @Valid AccessTokenRequest request) {

        String newAccessToken = accessTokenService.createNewAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
    }

    @Operation(summary = "JWT 토큰 검증",
            description = "Authorization 헤더의 액세스 토큰을 검증하고, 유효 시 사용자 ID를 반환합니다.")
    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validateToken(
            @RequestHeader("Authorization") @NotBlank String authHeader) {

        // "Bearer " 접두사 제거
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        if (tokenProvider.validateToken(token)) {
            String userId = tokenProvider.getClaims(token)
                    .get("id", String.class);
            return ResponseEntity.ok(Map.of("userId", userId));
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "유효하지 않은 토큰입니다."));
        }
    }
}