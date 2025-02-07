package capstone.dbfis.chatbot.domain.chatbot.service;

import capstone.dbfis.chatbot.domain.chatbot.entity.ChatMessage;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    // 채팅 메시지 저장
    @Async
    @Transactional
    public void saveMessage(String message, String response, String sender, ChatRoom chatRoom) {
        ChatMessage chatMessage = ChatMessage.builder()
                .message(message)
                .response(response)
                .sender(sender)
                .chatRoom(chatRoom)
                .build();
        chatMessageRepository.save(chatMessage);
    }

    // 특정 채팅방의 메시지 목록 불러오기
    public List<ChatMessage> getMessagesByChatRoom(ChatRoom chatRoom) {
        return chatMessageRepository.findByChatRoom(chatRoom);
    }

    // 특정 채팅방의 메시지 전부 삭제하기
    @Transactional
    public void deleteMessagesByChatRoom(ChatRoom chatRoom) {
        chatMessageRepository.deleteByChatRoom(chatRoom);
    }
}
