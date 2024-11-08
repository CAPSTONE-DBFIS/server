package capstone.dbfis.chatbot.domain.chatparticipant;

import java.io.Serializable;
import java.util.Objects;

public class ChatParticipantId implements Serializable {
    private String memberid;
    private String role;
    private String department;
    private String chatroomid;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatParticipantId chatParticipantId)) return false;
        return Objects.equals(memberid, chatParticipantId.memberid) &&
                Objects.equals(role, chatParticipantId.role) &&
                Objects.equals(department, chatParticipantId.department)&&
                Objects.equals(chatroomid, chatParticipantId.chatroomid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberid, role, department, chatroomid);

    }
}