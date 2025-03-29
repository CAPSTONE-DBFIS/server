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

    Optional<PostEmotion> findByPostAndMember(Post post, Member member);

    @Query("SELECT COUNT(pe) FROM PostEmotion pe WHERE pe.post = :post AND pe.emotion = :emotion")
    int countByPostAndEmotion(@Param("post") Post post, @Param("emotion") String emotion);
}
