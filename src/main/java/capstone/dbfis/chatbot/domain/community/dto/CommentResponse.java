package capstone.dbfis.chatbot.domain.community.dto;

import capstone.dbfis.chatbot.domain.community.entity.Comment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CommentResponse {
    private Long id;
    private String member;
    private String content;

    public CommentResponse(Comment comment){
        this.id = comment.getId();
        this.member = comment.getMember().getName();
        this.content = comment.getContent();
    }
}
