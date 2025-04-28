package capstone.dbfis.chatbot.domain.chatbot.dto;

import capstone.dbfis.chatbot.domain.project.dto.ProjectResponse;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDashboardDto {
    private List<ProjectResponse> projects;
    private Map<Long, List<ChatRoomDto>> projectChatrooms;
    private List<ChatRoomDto> personalChatrooms;
}
