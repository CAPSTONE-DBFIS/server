package capstone.dbfis.chatbot.domain.chatbot.repository;

import capstone.dbfis.chatbot.domain.chatbot.entity.ChatMessage;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoom(ChatRoom chatRoom); // 채팅방의 모든 메시지 조회
    void deleteByChatRoom(ChatRoom chatRoom); // 채팅방의 모든 메시지 삭제
}
