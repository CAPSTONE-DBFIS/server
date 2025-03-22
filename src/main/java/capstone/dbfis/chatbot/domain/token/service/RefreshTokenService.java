package capstone.dbfis.chatbot.domain.token.service;

import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.token.entity.RefreshToken;
import capstone.dbfis.chatbot.domain.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    // 주어진 memberId에 해당하는 RefreshToken을 찾는 메서드
    public Optional<RefreshToken> findByMemberId(String memberId) {
        // memberId로 RefreshToken 검색
        return refreshTokenRepository.findByMemberId(memberId);
    }

    // 주어진 refreshToken에 해당하는 RefreshToken을 찾는 메서드
    public Optional<RefreshToken> findByRefreshToken(String refreshToken) {
        // refreshToken으로 RefreshToken 검색
        return refreshTokenRepository.findByRefreshToken(refreshToken);
    }

    // 리프레시 토큰을 저장하는 메서드
    public void saveRefreshToken(Member member, String refreshToken) {
        refreshTokenRepository.save(new RefreshToken(member, refreshToken));
    }

    // 리프레시 토큰이 존재하면 업데이트, 없으면 저장하는 메서드
    public void updateOrSaveRefreshToken(Member member, String newRefreshToken) {
        refreshTokenRepository.findByMemberId(member.getId()).ifPresentOrElse(
                existingToken -> {
                    existingToken.setRefreshToken(newRefreshToken);
                    refreshTokenRepository.save(existingToken);
                },
                () -> saveRefreshToken(member, newRefreshToken)
        );
    }
}
