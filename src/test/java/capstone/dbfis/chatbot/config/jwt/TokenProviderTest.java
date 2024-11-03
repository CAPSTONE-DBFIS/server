package capstone.dbfis.chatbot.config.jwt;

import capstone.dbfis.chatbot.domain.member.Member;
import capstone.dbfis.chatbot.domain.member.MemberRepository;
import capstone.dbfis.chatbot.global.config.jwt.JwtProperties;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TokenProviderTest {
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JwtProperties jwtProperties;

    private Member testMember;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        // given
        testMember = memberRepository.save(Member.builder()
                .id("User1")
                .name("홍길동")
                .email("gildong@example.com")
                .phone("010-1234-5678")
                .nickname("gildong")
                .interests("Reading, Coding")
                .profileImage("default.png")
                .personaPreset(1)
                .build());
    }

    @Test
    void generateToken() {
        // when
        String token = tokenProvider.generateToken(testMember, Duration.ofDays(14));

        // then
        String MemberId = Jwts.parser()
                .setSigningKey(jwtProperties.getSecret())
                .parseClaimsJws(token)
                .getBody()
                .get("id", String.class);

        assertThat(token).isNotNull();
        assertThat(MemberId).isEqualTo(testMember.getId());
    }

    @Test
    void validateToken() {
        // given
        String expiredToken = tokenProvider.generateToken(testMember, Duration.ofDays(-14)); // 만료된 토큰
        String validToken = tokenProvider.generateToken(testMember, Duration.ofDays(14)); // 유효한 토큰

        // when
        boolean expiredResult = tokenProvider.validateToken(expiredToken);
        boolean validResult = tokenProvider.validateToken(validToken);

        // then
        assertThat(expiredResult).isFalse();
        assertThat(validResult).isTrue();
    }

    @Test
    void getAuthentication() {
        // given
        String token = tokenProvider.generateToken(testMember, Duration.ofDays(14));

        // when
        Authentication authentication = tokenProvider.getAuthentication(token);

        // then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(testMember.getEmail());
        assertThat(authentication.getCredentials()).isEqualTo(token);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void getMemberId() {
        // given
        String token = tokenProvider.generateToken(testMember, Duration.ofDays(14));

        // when
        String memberId = tokenProvider.getMemberId(token);

        // then
        // 정상 토큰 검증
        assertThat(memberId).isEqualTo(testMember.getId()); // 유저 ID 검증

        // 잘못된 토큰 검증
        String invalidToken = "invalid.token.value";
        assertThrows(IllegalStateException.class, () -> {
            tokenProvider.getMemberId(invalidToken);
        });
    }
}
