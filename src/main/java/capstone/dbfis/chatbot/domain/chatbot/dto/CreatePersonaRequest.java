package capstone.dbfis.chatbot.domain.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePersonaRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String prompt;

    private boolean preset = false;
}
