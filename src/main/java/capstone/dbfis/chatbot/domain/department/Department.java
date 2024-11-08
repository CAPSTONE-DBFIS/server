package capstone.dbfis.chatbot.domain.department;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder

public class Department {
    @Id
    @Column(name = "id", updatable = false)
    private String id;

    @Column(name = "department", nullable = false)
    private String department;

}
