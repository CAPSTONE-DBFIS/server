package capstone.dbfis.chatbot.domain.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateProjectRequest {
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
