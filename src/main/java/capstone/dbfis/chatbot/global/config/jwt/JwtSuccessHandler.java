package capstone.dbfis.chatbot.global.config.jwt;

import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.domain.token.entity.RefreshToken;
import capstone.dbfis.chatbot.domain.token.service.RefreshTokenService;
import capstone.dbfis.chatbot.global.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JwtSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(24);
    public static final String REDIRECT_PATH = "/";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String memberId = authentication.getName();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("invalid Member"));

        // 리프레쉬 토큰 생성 후, db에 저장
        String refreshToken = tokenProvider.generateToken(member, REFRESH_TOKEN_DURATION);

        // 리프레시 토큰을 데이터베이스에 저장
        Optional<RefreshToken> existingToken = refreshTokenService.findByMemberId(memberId);

        if (existingToken.isPresent()) {
            // 이미 존재하는 경우 업데이트
            refreshTokenService.updateRefreshToken(member, refreshToken);
        } else {
            // 존재하지 않는 경우 새로 저장
            refreshTokenService.saveRefreshToken(member, refreshToken);
        }

        // 쿠키에 저장
        addRefreshTokenToCookie(request, response, refreshToken);

        // 인증 관련 속성 정리
        super.clearAuthenticationAttributes(request);

        // 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, REDIRECT_PATH);
    }

    // 생성된 리프레시 토큰을 쿠키에 저장
    private void addRefreshTokenToCookie(HttpServletRequest request,
                                         HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }
}
