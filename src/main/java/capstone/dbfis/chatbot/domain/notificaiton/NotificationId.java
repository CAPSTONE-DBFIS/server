package capstone.dbfis.chatbot.domain.notificaiton;

import capstone.dbfis.chatbot.domain.chatparticipant.ChatParticipantId;

import java.io.Serializable;
import java.util.Objects;

public class NotificationId implements Serializable {
    private String id;
    private String memberid;
    private String role;
    private String department;
    private String scheduleid;
    private String messageid;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationId notificationId)) return false;
        return Objects.equals(id, notificationId.id) &&
                Objects.equals(memberid, notificationId.memberid) &&
                Objects.equals(role, notificationId.role) &&
                Objects.equals(department, notificationId.department)&&
                Objects.equals(scheduleid, notificationId.scheduleid)&&
                Objects.equals(messageid, notificationId.messageid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, memberid, role, department, scheduleid ,messageid);

    }
}
