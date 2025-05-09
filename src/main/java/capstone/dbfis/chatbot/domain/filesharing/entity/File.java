package capstone.dbfis.chatbot.domain.filesharing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class File {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String uploaderId;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Builder.Default
    @Column(nullable = false)
    private int downloadCount = 0;
}