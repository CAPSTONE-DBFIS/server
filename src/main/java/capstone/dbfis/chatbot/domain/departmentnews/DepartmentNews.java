package capstone.dbfis.chatbot.domain.departmentnews;

import capstone.dbfis.chatbot.domain.department.Department;
import capstone.dbfis.chatbot.domain.news.News;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder

public class DepartmentNews {

    @EmbeddedId
    private DepartmentNewsId id;

    @MapsId("news_title")
    @ManyToOne
    @JoinColumn(name = "title", updatable = false)
    private News news;

    @MapsId("departmentId")
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

}
