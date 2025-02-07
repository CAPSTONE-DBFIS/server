package capstone.dbfis.chatbot.domain.chatbot.repository;

import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findAllByMemberId(String userId); // 사용자의 모든 채팅방 조회
    Optional<ChatRoom> findByIdAndMemberId(Long id, String userId); // 특정 chatroomId + userId로 채팅방 조회
}
