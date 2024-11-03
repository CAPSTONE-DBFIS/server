package capstone.dbfis.chatbot.config.jwt;

import capstone.dbfis.chatbot.domain.member.Member;
import capstone.dbfis.chatbot.domain.member.MemberRepository;
import capstone.dbfis.chatbot.domain.token.dto.CreateAccessTokenRequest;
import capstone.dbfis.chatbot.domain.token.model.RefreshToken;
import capstone.dbfis.chatbot.domain.token.repository.RefreshTokenRepository;
import capstone.dbfis.chatbot.global.config.jwt.JwtProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TokenApiControllerTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    JwtProperties jwtProperties;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    public void mockMvcSetup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    public void createNewAccessToken() throws Exception {
        //given
        final String url = "/api/token";

        Member testMember = memberRepository.save(Member.builder()
                .id("User1")
                .name("홍길동")
                .email("gildong@example.com")
                .phone("010-1234-5678")
                .nickname("gildong")
                .interests("Reading, Coding")
                .profileImage("default.png")
                .personaPreset(1)
                .build());

        String refreshToken = JwtFactory.builder()
                .claims(Map.of("id", testMember.getId())).build().createToken(jwtProperties);

        refreshTokenRepository.save(new RefreshToken(testMember, refreshToken));

        CreateAccessTokenRequest req = new CreateAccessTokenRequest();
        req.setRefreshToken(refreshToken);
        final String requestBody = objectMapper.writeValueAsString(req);

        // when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

    }
}
