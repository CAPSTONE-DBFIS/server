package capstone.dbfis.chatbot.domain.notificaiton;

import capstone.dbfis.chatbot.domain.message.Message;
import capstone.dbfis.chatbot.domain.schedule.Schedule;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
@IdClass(NotificationId.class)
public class Notification {
    @Id
    @Column(name = "id",updatable = false)
    private String id;

    @Id
    @Column(name = "memberid")
    private String memberid;

    @Id
    @Column(name = "role")
    private String role;

    @Id
    @Column(name = "department")
    private String department;

    @Id
    @Column(name = "scheduleid")
    private String scheduleid;

    @Id
    @Column(name = "messageid")
    private String messageid;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "memberid", referencedColumnName = "memberid"),
            @JoinColumn(name = "role", referencedColumnName = "role"),
            @JoinColumn(name = "department", referencedColumnName = "department"),
            @JoinColumn(name = "scheduleid", referencedColumnName = "id")
    })
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "messageid", referencedColumnName = "id")
    private Message message;

    @Column(name = "type")
    private String type;

    @Column(name = "time")
    private LocalDateTime time;

    @Column(name = "isRead")
    private boolean isRead;
}
