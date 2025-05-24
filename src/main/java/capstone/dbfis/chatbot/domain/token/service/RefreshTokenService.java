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
        try {
            // memberId로 RefreshToken 검색
            return refreshTokenRepository.findByMemberId(memberId);
        } catch (Exception e) {
            throw new IllegalStateException("RefreshToken 검색 중 오류가 발생했습니다.", e);
        }
    }

    // 주어진 refreshToken에 해당하는 RefreshToken을 찾는 메서드
    public Optional<RefreshToken> findByRefreshToken(String refreshToken) {
        try {
            // refreshToken으로 RefreshToken 검색
            return refreshTokenRepository.findByRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new IllegalStateException("리프레시 토큰 검색 중 오류가 발생했습니다.", e);
        }
    }

    // 리프레시 토큰을 저장하는 메서드
    public void saveRefreshToken(Member member, String refreshToken) {
        try {
            refreshTokenRepository.save(new RefreshToken(member, refreshToken));
        } catch (Exception e) {
            throw new IllegalStateException("리프레시 토큰 저장 중 오류가 발생했습니다.", e);
        }
    }

    // 리프레시 토큰이 존재하면 업데이트, 없으면 저장하는 메서드
    public void updateOrSaveRefreshToken(Member member, String newRefreshToken) {
        try {
            refreshTokenRepository.findByMemberId(member.getId()).ifPresentOrElse(
                    existingToken -> {
                        existingToken.setRefreshToken(newRefreshToken);
                        refreshTokenRepository.save(existingToken);
                    },
                    () -> saveRefreshToken(member, newRefreshToken)
            );
        } catch (Exception e) {
            throw new IllegalStateException("리프레시 토큰 업데이트 또는 저장 중 오류가 발생했습니다.", e);
        }
    }
}