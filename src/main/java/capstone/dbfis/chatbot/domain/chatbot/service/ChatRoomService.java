package capstone.dbfis.chatbot.domain.chatbot.service;

import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoomType;
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
    public ChatRoom createChatRoom(String memberId, ChatRoomType type, Long projectId) {
        ChatRoom chatRoom = ChatRoom.builder()
                .memberId(memberId)
                .name("새 채팅방") // 임시 이름 부여
                .type(type)
                .projectId(projectId)
                .build();
        return chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public void updateChatRoomName(String memberId, Long chatRoomId, String newName) {
        // 채팅방 접근 권한 검증, 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByIdAndMemberId(chatRoomId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        if (newName != null && !newName.isBlank()) {
            chatRoom.updateName(newName);
            chatRoomRepository.save(chatRoom);
        }
    }

    // 특정 사용자에 대한 채팅방 리스트 조회
    public List<ChatRoom> getChatRoomsByMemberId(String memberId) {
        return chatRoomRepository.findByMemberIdOrderByIdAsc(memberId);
    }


    // 특정 사용자와 채팅방 ID에 대한 대한 채팅방 조회
    public ChatRoom getChatRoomByIdAndMemberId(Long chatroomId, String memberId) {
        // 채팅방 접근 권한 검증, 채팅방 조회
        return chatRoomRepository.findByIdAndMemberId(chatroomId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방을 찾을 수 없습니다."));
    }

    // 채팅방 삭제
    @Transactional
    public void deleteChatRoomAndMessages(Long chatroomId, String memberId) {
        // 채팅방 접근 권한 검증, 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByIdAndMemberId(chatroomId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방을 찾을 수 없습니다."));

        // 해당 채팅방의 모든 메시지 삭제
        chatMessageService.deleteMessagesByChatRoom(chatRoom);

        // 채팅방 삭제
        chatRoomRepository.delete(chatRoom);
    }
}