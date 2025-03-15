package capstone.dbfis.chatbot.domain.community.repository;

import capstone.dbfis.chatbot.domain.community.entity.PostEmotion;
import capstone.dbfis.chatbot.domain.community.entity.Post;
import capstone.dbfis.chatbot.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmotionRepository extends JpaRepository<PostEmotion, Long> {
    boolean existsByPostAndMember(Post post, Member member);
    void deleteByPostAndMember(Post post, Member member);
    int countByPost(Post post);

    Optional<PostEmotion> findByPostAndMember(Post post, Member member);

    // 특정 게시글에서 특정 감정이 몇 개 있는지 조회
    @Query("SELECT COUNT(pe) FROM PostEmotion pe WHERE pe.post = :post AND pe.emotion = :emotion")
    int countByPostAndEmotion(@Param("post") Post post, @Param("emotion") String emotion);
}
