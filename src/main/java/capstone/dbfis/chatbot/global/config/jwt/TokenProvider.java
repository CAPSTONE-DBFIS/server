package capstone.dbfis.chatbot.global.config.jwt;

import capstone.dbfis.chatbot.domain.member.entity.Member;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenProvider {

    private final JwtProperties jwtProperties;

    // JWT 토큰 생성
    public String generateToken(Member member, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), member);
    }

    // JWT 토큰 생성 로직
    private String makeToken(Date expiry, Member member) {
        try {
            Date now = new Date();
            log.info("JWT Secret Key in makeToken: {}", jwtProperties.getSecret());
            return Jwts.builder()
                    .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                    .setIssuer(jwtProperties.getIssuer())
                    .setIssuedAt(now)
                    .setExpiration(expiry)
                    .setSubject(String.valueOf(member.getId())) // String 변환
                    .claim("id", String.valueOf(member.getId())) // String 변환
                    .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret())
                    .compact();
        } catch (JwtException e) {
            log.error("Error creating JWT token: {}", e.getMessage());
            throw new RuntimeException("Error creating JWT token");
        }
    }

    // JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            token = removeBearerPrefix(token);
            log.info("JWT Secret Key in validateToken: {}", jwtProperties.getSecret());
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecret())
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT Token이 만료되었습니다. 유효기간: {}", e.getClaims().getExpiration());
        } catch (UnsupportedJwtException e) {
            log.error("JWT Token이 지원되지 않습니다. {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT Token이 손상되었습니다. {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("JWT Token의 서명이 유효하지 않습니다. {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT Token 유효성 검증에 실패하였습니다. {}", e.getMessage());
        }
        return false;
    }

    // JWT 토큰 기반 인증 정보 반환
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        if (claims == null || claims.getSubject() == null) {
            throw new IllegalStateException("유효하지 않은 토큰입니다.");
        }
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities),
                token,
                authorities
        );
    }

    // JWT에서 클레임 정보 추출
    public Claims getClaims(String token) {
        try {
            token = removeBearerPrefix(token);
            log.info("Parsing JWT Token: {}", token);
            return Jwts.parser()
                    .setSigningKey(jwtProperties.getSecret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("JWT 만료됨: {}", e.getMessage());
            throw new IllegalStateException("토큰이 만료되었습니다.");
        } catch (SignatureException e) {
            log.error("JWT 서명 불일치: {}", e.getMessage());
            throw new IllegalStateException("토큰 서명이 유효하지 않습니다.");
        } catch (JwtException e) {
            log.error("JWT 파싱 오류: {}", e.getMessage());
            throw new IllegalStateException("유효하지 않은 토큰입니다.");
        }
    }

    // JWT 토큰에서 Member ID 반환
    public String getMemberId(String token) {
        Claims claims = getClaims(token);
        if (claims == null || claims.get("id", String.class) == null) {
            throw new IllegalStateException("Member ID가 토큰에 존재하지 않습니다.");
        }
        return claims.get("id", String.class);
    }

    // "Bearer " 제거 함수
    private String removeBearerPrefix(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}