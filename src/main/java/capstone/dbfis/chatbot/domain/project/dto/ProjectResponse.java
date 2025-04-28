package capstone.dbfis.chatbot.domain.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private Long teamId;
    private LocalDate startDate;
    private LocalDate endDate;
}
