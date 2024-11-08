package capstone.dbfis.chatbot.domain.reaction;

import java.io.Serializable;
import java.util.Objects;

public class ReactionId implements Serializable {
    private String commentid;
    private String postid;
    private String memberid;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReactionId that = (ReactionId) o;
        return Objects.equals(commentid, that.commentid) &&
                Objects.equals(postid, that.postid) &&
                Objects.equals(memberid, that.memberid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentid, postid, memberid);
    }
}
