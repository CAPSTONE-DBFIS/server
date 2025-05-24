package capstone.dbfis.chatbot.domain.chatbot.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDashboardDto {
    private List<ChatRoomDto> personalChatrooms;
    private List<ChatRoomDto> teamChatrooms;
    private List<TeamDto> myTeams;
}
