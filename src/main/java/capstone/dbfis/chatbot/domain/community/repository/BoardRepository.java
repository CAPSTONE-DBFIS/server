package capstone.dbfis.chatbot.domain.community.repository;

import capstone.dbfis.chatbot.domain.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BoardRepository extends JpaRepository<Post,Long> {
}
