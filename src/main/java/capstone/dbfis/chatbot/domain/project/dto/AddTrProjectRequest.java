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

    @Schema(description="프로젝트 설명", example="설명입니다.")
    private String description;

    @NotNull @Schema(description="시작일(YYYY-MM-DD)", example="2025-05-01")
    private LocalDate startDate;

    @NotNull @Schema(description="종료일(YYYY-MM-DD)", example="2025-05-31")
    private LocalDate endDate;
}