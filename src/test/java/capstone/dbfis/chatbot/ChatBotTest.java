package capstone.dbfis.chatbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatBotTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        // 필요시 테스트 데이터 초기화
    }

    @Test
    public void testChatBotResponse() throws Exception {
        String prompt = "딥러닝에 대해 설명해줘"; // 요청할 프롬프트

        // API 호출 및 결과 검증
        ResultActions resultActions = mockMvc.perform(get("/bot/chat")
                .param("prompt", prompt)
                .contentType(MediaType.APPLICATION_JSON));

        // 예상 응답과 실제 응답 검증
        resultActions
                .andExpect(status().isOk()) // 상태 코드가 200인지 확인
                .andExpect(jsonPath("$.response").exists()) // 응답에 response 필드가 존재하는지 확인
                .andExpect(jsonPath("$.response").isString()); // 응답의 response 필드가 문자열인지 확인
    }
}