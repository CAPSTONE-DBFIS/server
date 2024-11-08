package capstone.dbfis.chatbot.domain.token.service;

import capstone.dbfis.chatbot.domain.token.model.RefreshToken;
import capstone.dbfis.chatbot.domain.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    // 주어진 memberId에 해당하는 RefreshToken을 찾는 메서드
    public RefreshToken findByMemberId(String memberId) {
        // memberId로 RefreshToken 검색
        return refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    }

    // 주어진 refreshToken에 해당하는 RefreshToken을 찾는 메서드
    public RefreshToken findByRefreshToken(String refreshToken) {
        // refreshToken으로 RefreshToken 검색
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    }
}
