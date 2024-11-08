package capstone.dbfis.chatbot.domain.post;

import capstone.dbfis.chatbot.domain.member.MemberId;

import java.io.Serializable;
import java.util.Objects;

public class PostId implements Serializable {

    private String id;
    private String memberid;
    private String role;
    private String department;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostId postId)) return false;
        return Objects.equals(id, postId.id) &&
                Objects.equals(memberid, postId.memberid) &&
                Objects.equals(role, postId.role)&&
                Objects.equals(department, postId.department);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, memberid, role, department);

    }
}
