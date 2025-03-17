package capstone.dbfis.chatbot.global.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    // 특정 경로에서는 필터를 건너뛴다.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean shouldSkip = path.equals("/") || path.startsWith("/login") || path.startsWith("/signup");

//        System.out.println("[JWT FILTER] 필터 건너뛰기 여부: " + shouldSkip + " (요청 URL: " + path + ")");
        return shouldSkip;
    }

    // Authorization 헤더에서 액세스 토큰을 추출하는 메서드
    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    // HTTP 요청을 필터링하는 메서드
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        System.out.println("[JWT FILTER] 요청 URL: " + request.getRequestURI());

        // Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        System.out.println("[JWT FILTER] Authorization 헤더: " + authorizationHeader);

        String token = getAccessToken(authorizationHeader);
        System.out.println("[JWT FILTER] 추출된 토큰: " + token);

        if (token == null || token.isEmpty()) {
            System.out.println("[JWT FILTER] 토큰이 없음. 요청을 계속 진행합니다.");
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 유효성 검증
        if (tokenProvider.validateToken(token)) {
            System.out.println("[JWT FILTER] 토큰 유효성 검증 성공. 사용자 인증 정보 설정 중...");

            Authentication authentication = tokenProvider.getAuthentication(token);
            System.out.println("[JWT FILTER] 사용자 인증 정보: " + authentication.getName());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            System.out.println("[JWT FILTER] 토큰 유효성 검증 실패.");
        }

        // 다음 필터로 요청과 응답을 전달
        filterChain.doFilter(request, response);
    }
}
