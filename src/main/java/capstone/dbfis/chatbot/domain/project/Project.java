package capstone.dbfis.chatbot.domain.project;

import capstone.dbfis.chatbot.domain.department.Department;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder

@IdClass(ProjectId.class)
public class Project {

    @Id
    @Column(name = "id", updatable = false)
    private String id;

    @Id
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "startAt", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "endAt", nullable = false)
    private LocalDateTime endAt;

}
