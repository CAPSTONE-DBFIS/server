package capstone.dbfis.chatbot.domain.project;

import capstone.dbfis.chatbot.domain.member.MemberId;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

public class ProjectId implements Serializable {
    private String id;
    private String department;


    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectId projectId)) return false;
        return Objects.equals(id, projectId.id) &&
                Objects.equals(department, projectId.department);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, department);
    }
}
