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

    // JWT 토큰 생성 로직
    public String generateToken(Member member, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), member);
    }

    // JWT 토큰 생성 메서드
    private String makeToken(Date expiry, Member member) {
        try {
            Date now = new Date();
            return Jwts.builder()
                    .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                    .setIssuer(jwtProperties.getIssuer())
                    .setIssuedAt(now)
                    .setExpiration(expiry)
                    .setSubject(member.getEmail())
                    .claim("id", member.getId())
                    // 서명: 비밀값과 함께 해시값을 HS256 방식으로 암호화
                    .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret())
                    .compact();
        } catch (JwtException e) {
            log.error("Error creating JWT token");
            throw new RuntimeException("Error creating JWT token");
        }
    }

    // JWT 토큰 유효성 검증 메서드
    public boolean validateToken(String token) {
        try {
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

    // JWT 토큰 기반으로 인증 정보(이메일)를 가져오는 메서드
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        if (claims == null || claims.getSubject() == null) {
            throw new IllegalStateException("유효하지 않은 토큰입니다.");
        }
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(new org.springframework.
                security.core.userdetails.User(claims.getSubject(), "", authorities), token, authorities);
    }

    // JWT 토큰에서 클레임 정보를 추출하는 메서드
    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtProperties.getSecret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("토큰으로부터 클레임 정보를 가져오는데 실패했습니다.");
            return null;
        }

    }

    // JWT 토큰에서 member id를 가져옴
    public String getMemberId(String token) {
        Claims claims = getClaims(token);
        if (claims == null || claims.get("id", String.class) == null) {
            throw new IllegalStateException("Member ID가 토큰에 존재하지 않습니다.");
        }
        return claims.get("id", String.class);
    }
}
