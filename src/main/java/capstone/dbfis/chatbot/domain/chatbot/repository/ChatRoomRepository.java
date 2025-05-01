package capstone.dbfis.chatbot.domain.chatbot.repository;

import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("SELECT c FROM ChatRoom c WHERE c.memberId = :memberId " +
            "ORDER BY c.favorite DESC, c.favoriteAddedAt DESC NULLS LAST, c.id ASC")
    List<ChatRoom> findByMemberIdSorted(@Param("memberId") String memberId);
    List<ChatRoom> findByMemberIdOrderByIdAsc(String memberId); // 사용자의 모든 채팅방을 조회 후 채팅방 id 기준 오름차순 정렬
    Optional<ChatRoom> findByIdAndMemberId(Long id, String userId); // 특정 chatroomId + userId로 채팅방 조회
}
