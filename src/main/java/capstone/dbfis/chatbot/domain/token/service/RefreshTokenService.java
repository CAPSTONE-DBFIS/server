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

    public void saveRefreshToken(Member member, String refreshToken) {
        refreshTokenRepository.save(new RefreshToken(member, refreshToken));
    }

    // 리프레시 토큰 업데이트 메서드
    public void updateRefreshToken(Member member, String newRefreshToken) {
        Optional<RefreshToken> optionalRefreshToken = findByMemberId(member.getId());

        // RefreshToken이 존재하는지 체크
        if (optionalRefreshToken.isPresent()) {
            RefreshToken refreshToken = optionalRefreshToken.get();
            refreshToken.update(newRefreshToken); // 기존 리프레시 토큰을 새 토큰으로 업데이트
            refreshTokenRepository.save(refreshToken); // 기존 토큰을 업데이트한 후 저장
        } else {
            throw new IllegalArgumentException("Invalid member ID");
        }
    }
}
