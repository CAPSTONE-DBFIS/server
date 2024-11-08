package capstone.dbfis.chatbot.domain.comment;

import capstone.dbfis.chatbot.domain.post.PostId;

import java.io.Serializable;
import java.util.Objects;

public class CommentId implements Serializable {
    private String id;
    private String memberid;
    private String role;
    private String department;
    private String postid;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommentId commentId)) return false;
        return Objects.equals(id, commentId.id) &&
                Objects.equals(memberid, commentId.memberid) &&
                Objects.equals(role, commentId.role)&&
                Objects.equals(department, commentId.department)&&
                Objects.equals(postid, commentId.postid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, memberid, role, department, postid);

    }
}
