package capstone.dbfis.chatbot.domain.team.project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddProjectRequest {
    private Long teamId;
    private String name;
    private String description;
}
