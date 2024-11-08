package capstone.dbfis.chatbot.domain.departmentnews;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class DepartmentNewsId implements Serializable {
    private String news_title;
    private String departmentId;

}
