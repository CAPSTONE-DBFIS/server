package capstone.dbfis.chatbot.domain.chatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "persona")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;          // e.g., "트렌드 분석 전문가"

    @Column(nullable = false, length = 1000)
    private String prompt;        // 프롬프트 텍스트

    @Column(nullable = false)
    private boolean preset;       // true = 기본 preset, false = 사용자 커스텀

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private String ownerId;       // member_id (preset이면 'SYSTEM')

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
