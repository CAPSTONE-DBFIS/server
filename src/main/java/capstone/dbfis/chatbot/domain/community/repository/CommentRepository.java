package capstone.dbfis.chatbot.domain.community.repository;

import capstone.dbfis.chatbot.domain.community.entity.Comment;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaRepository;

=======
import capstone.dbfis.chatbot.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
>>>>>>> e5e3669776904733c2b1ebb8b916513f36c41bc7

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
<<<<<<< HEAD
    List<Comment> findByPostId(Long postId);
=======
    List<Comment> findByPostId(Long postId);// 특정 게시글에 달린 댓글 목록 조회
>>>>>>> e5e3669776904733c2b1ebb8b916513f36c41bc7
}
