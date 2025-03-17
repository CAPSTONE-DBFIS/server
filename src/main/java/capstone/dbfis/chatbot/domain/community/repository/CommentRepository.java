package capstone.dbfis.chatbot.domain.community.repository;

import capstone.dbfis.chatbot.domain.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
}
