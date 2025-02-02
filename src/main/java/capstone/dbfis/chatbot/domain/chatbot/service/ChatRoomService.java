package capstone.dbfis.chatbot.domain.chatbot.service;

import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageService chatMessageService;


    // 새로운 채팅방 생성
    @Transactional
    public ChatRoom createChatRoom(String userId) {
        ChatRoom chatRoom = ChatRoom.builder()
                .userId(userId)
                .build();
        return chatRoomRepository.save(chatRoom);
    }


    // 특정 사용자에 대한 채팅방 리스트 조회
    public List<ChatRoom> getChatRoomsByUserId(String userId) {
        return chatRoomRepository.findAllByUserId(userId);
    }


    // 특정 사용자와 채팅방 ID에 대한 대한 채팅방 조회
    public ChatRoom getChatRoomByIdAndUserId(Long chatroomId, String userId) {
        return chatRoomRepository.findByIdAndUserId(chatroomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방을 찾을 수 없습니다."));
    }

    // 채팅방 삭제
    @Transactional
    public void deleteChatRoomAndMessages(Long chatroomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndUserId(chatroomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방을 찾을 수 없습니다."));

        // 해당 채팅방의 모든 메시지 삭제
        chatMessageService.deleteMessagesByChatRoom(chatRoom);

        // 채팅방 삭제
        chatRoomRepository.delete(chatRoom);
    }
}