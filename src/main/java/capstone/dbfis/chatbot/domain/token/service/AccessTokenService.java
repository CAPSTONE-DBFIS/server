package capstone.dbfis.chatbot.domain.token.service;

import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.token.entity.RefreshToken;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class AccessTokenService {
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberRepository memberRepository;

    // 새로운 액세스 토큰을 생성하는 메서드
    public String createNewAccessToken(String refreshToken) {
        // 리프레시 토큰이 유효한지 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");
        }

        // 리프레시 토큰을 사용하여 해당 멤버의 ID를 찾음
        RefreshToken storedRefreshToken = refreshTokenService.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다."));

        String memberId = storedRefreshToken.getMember().getId();

        // 멤버 ID를 사용하여 멤버 정보를 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원이 존재하지 않습니다."));

        // 멤버 정보를 기반으로 새로운 액세스 토큰 생성 및 반환
        return tokenProvider.generateToken(member, Duration.ofDays(365));
    }
}
