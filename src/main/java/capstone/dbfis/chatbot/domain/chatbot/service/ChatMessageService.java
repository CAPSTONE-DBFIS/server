package capstone.dbfis.chatbot.domain.chatbot.service;

import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatMessage;
import capstone.dbfis.chatbot.domain.chatbot.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * 특정 채팅방의 메시지를 시간순으로 조회
     */
    public List<ChatMessage> getMessagesByChatRoom(ChatRoom chatRoom) {
        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
    }

    /**
     * 특정 채팅방에 속한 모든 메시지를 삭제 (트랜잭션 보장)
     */
    @Transactional
    public void deleteMessagesByChatRoom(ChatRoom chatRoom) {
        chatMessageRepository.deleteByChatRoom(chatRoom);
    }
}