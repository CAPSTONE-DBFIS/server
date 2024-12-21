package capstone.dbfis.chatbot;

import capstone.dbfis.chatbot.domain.member.dto.AddMemberRequest;
import capstone.dbfis.chatbot.domain.member.repository.EmailVerificationRepository;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class MemberApiIntegrationTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        emailVerificationRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void testSignupAndEmailVerificationFlow() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        String uniqueId = "testUser" + System.currentTimeMillis();

        // 1. 회원가입 요청
        AddMemberRequest request = new AddMemberRequest();
        request.setId(uniqueId);
        request.setPassword("password123");
        request.setName("Test User");
        request.setEmail(uniqueId + "@example.com");
        request.setPhone("01012345678");
        request.setNickname("testNickname");
        request.setDepartment("testDepartment");
        request.setInterests("coding,testing");
        request.setPersona_preset("1");

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 2. 이메일 인증 코드 발송 확인
        var emailVerification = emailVerificationRepository.findByMemberId(uniqueId)
                .orElseThrow(() -> new IllegalStateException("Email verification not found"));

        // 3. 이메일 인증 확인 (POST 요청)
        mockMvc.perform(post("/verify-email")
                        .param("verificationCode", emailVerification.getVerificationCode()))
                .andExpect(status().isOk())
                .andExpect(content().string("이메일 인증이 완료되었습니다."));

        // 4. 인증 후 멤버 상태 확인
        var verifiedMember = memberRepository.findById(uniqueId)
                .orElseThrow(() -> new IllegalStateException("Member not found"));

        assert verifiedMember.isEnabled(); // Email 인증 후 계정 활성화
    }
}