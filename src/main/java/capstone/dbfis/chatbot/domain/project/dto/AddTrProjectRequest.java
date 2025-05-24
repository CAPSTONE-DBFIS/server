package capstone.dbfis.chatbot.domain.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AddTrProjectRequest {
    @NotNull
    @Schema(description="팀 ID", example="1")
    private Long teamId;

    @NotNull @Schema(description="프로젝트 이름", example="새 프로젝트")
    private String name;
}