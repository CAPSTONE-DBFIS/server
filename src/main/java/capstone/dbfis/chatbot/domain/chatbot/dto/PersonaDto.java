package capstone.dbfis.chatbot.domain.chatbot.dto;

import lombok.*;

/**
 * 챗봇 페르소나 정보 전송용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaDto {
    private Long id;
    private String name;
    private String prompt;
    private boolean preset;
}
