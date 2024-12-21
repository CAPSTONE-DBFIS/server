package capstone.dbfis.chatbot.domain.token.service;

import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.service.MemberService;
import capstone.dbfis.chatbot.domain.token.entity.RefreshToken;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class AccessTokenService {
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberService memberService;

    // 새로운 액세스 토큰을 생성하는 메서드
    public String createNewAccessToken(String refreshToken) {
        // 리프레시 토큰이 유효한지 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // 리프레시 토큰을 사용하여 해당 멤버의 ID를 찾음
        RefreshToken storedRefreshToken = refreshTokenService.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        String memberId = storedRefreshToken.getMember().getId();

        // 멤버 ID를 사용하여 멤버 정보를 조회
        Member member = memberService.findById(memberId);

        // 멤버 정보를 기반으로 새로운 액세스 토큰 생성 및 반환 (2시간 유효)
        return tokenProvider.generateToken(member, Duration.ofHours(2));
    }
}
