package capstone.dbfis.chatbot.domain.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePersonaRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String prompt;

    private boolean active;
}