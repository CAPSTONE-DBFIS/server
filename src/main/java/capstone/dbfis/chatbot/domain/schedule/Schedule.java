package capstone.dbfis.chatbot.domain.schedule;

import capstone.dbfis.chatbot.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
@IdClass(ShceduleId.class)

public class Schedule {
    @Id
    @Column(name = "id")
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

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "memberid", referencedColumnName = "id"),
            @JoinColumn(name = "role", referencedColumnName = "role_id"),
            @JoinColumn(name = "department", referencedColumnName = "department_id")
    })
    private Member member;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime start_date;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime start_time;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime end_date;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime end_time;

    @Column(name = "location", nullable = false)
    private String location;
}
