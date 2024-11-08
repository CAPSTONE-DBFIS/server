package capstone.dbfis.chatbot.domain.searchhistory;

import capstone.dbfis.chatbot.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
@IdClass(SearchHistoryId.class)
public class SearchHistory {

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

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "searchedAt", nullable = false)
    private LocalDateTime searchedAt;
}
