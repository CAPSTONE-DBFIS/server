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

    // Authorization 헤더에서 액세스 토큰을 추출하는 메서드
    private String getAccessToken(String authorizationHeader) {
        // 헤더가 null이 아니고, Bearer 접두사로 시작하는지 확인
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            // 접두사를 제거한 토큰 반환
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null; // 유효한 토큰이 없을 경우 null 반환
    }

    // HTTP 요청을 필터링하는 메서드
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)  throws ServletException, IOException {

        // Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        String token = getAccessToken(authorizationHeader);


        // /api/** 경로에 대해 인증을 생략 (중요: 개발 과정 중에서만 허용, 추후 수정 필요)
        if (request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰이 null이거나 비어있는 경우, 검증을 생략하고 다음 필터로 이동
        if (token == null || token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 유효성 검증
        if (tokenProvider.validateToken(token)) {
            // 유효한 토큰일 경우 인증 정보 가져오기
            Authentication authentication = tokenProvider.getAuthentication(token);
            // SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 다음 필터로 요청과 응답을 전달
        filterChain.doFilter(request, response);
    }
}
