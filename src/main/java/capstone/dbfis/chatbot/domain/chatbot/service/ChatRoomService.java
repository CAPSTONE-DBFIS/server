package capstone.dbfis.chatbot.domain.chatbot.service;

import capstone.dbfis.chatbot.domain.chatbot.dto.ChatDashboardDto;
import capstone.dbfis.chatbot.domain.chatbot.dto.ChatRoomDto;
import capstone.dbfis.chatbot.domain.chatbot.dto.TeamDto;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoomType;
import capstone.dbfis.chatbot.domain.chatbot.repository.ChatRoomRepository;
import capstone.dbfis.chatbot.domain.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageService chatMessageService;
    private final TeamService teamService;

    /**
     * 새로운 채팅방 생성 및 DTO 반환 (생성된 채팅방은 가장 최근에 접근한 것처럼 정렬 우선순위 고려)
     */
    @Transactional
    public ChatRoomDto createChatRoomAndReturnDto(String memberId, ChatRoomType type, Long teamId) {
        ChatRoom chatRoom = ChatRoom.builder()
                .memberId(memberId)
                .name("새 채팅방") // 임시 이름
                .type(type)
                .teamId(teamId)
                .createdAt(LocalDateTime.now())
                .build();
        chatRoom = chatRoomRepository.save(chatRoom);

        return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .type(chatRoom.getType())
                .teamId(chatRoom.getTeamId())
                .favorite(chatRoom.isFavorite())
                .favoriteAddedat(chatRoom.getFavoriteAddedAt())
                .build();
    }

    /**
     * 채팅방 대시보드 DTO 구성
     */
    public ChatDashboardDto buildChatDashboard(String memberId) {
        // 1. 해당 사용자에 속한 전체 채팅방 조회
        List<ChatRoom> rooms = getChatRoomsByMemberId(memberId);

        // 2. 추가일 최신순 > 생성일 최신순 정렬 (즐겨찾기 여부 무시)
        rooms.sort((r1, r2) -> {
            LocalDateTime time1 = Optional.ofNullable(r1.getFavoriteAddedAt()).orElse(r1.getCreatedAt());
            LocalDateTime time2 = Optional.ofNullable(r2.getFavoriteAddedAt()).orElse(r2.getCreatedAt());
            return time2.compareTo(time1); // 최신순 정렬
        });

        // 3. ChatRoom → ChatRoomDto로 변환
        List<ChatRoomDto> roomDtos = rooms.stream()
                .map(r -> ChatRoomDto.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .type(r.getType())
                        .teamId(r.getTeamId())
                        .favorite(r.isFavorite())
                        .favoriteAddedat(r.getFavoriteAddedAt())
                        .build())
                .toList();

        // 4. 개인 채팅방과 팀 채팅방 분리
        List<ChatRoomDto> personalRooms = roomDtos.stream()
                .filter(dto -> dto.getTeamId() == null)
                .toList();

        List<ChatRoomDto> teamChatList = roomDtos.stream()
                .filter(dto -> dto.getTeamId() != null)
                .toList();

        // 5. 속해있는 팀 정보 조회
        List<TeamDto> myTeams = teamService.getUserTeams(memberId).stream()
                .map(t -> TeamDto.builder()
                        .teamId(t.getTeamId())
                        .teamName(t.getTeamName())
                        .build())
                .toList();

        // 6. 최종 응답 DTO 생성
        return ChatDashboardDto.builder()
                .personalChatrooms(personalRooms)
                .teamChatrooms(teamChatList)
                .myTeams(myTeams)
                .build();
    }

    /**
     * 채팅방 이름 변경
     */
    @Transactional
    public void updateChatRoomName(String memberId, Long chatRoomId, String newName) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndMemberId(chatRoomId, memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "채팅방을 찾을 수 없습니다."));

        if (newName != null && !newName.isBlank()) {
            chatRoom.updateName(newName);
            chatRoomRepository.save(chatRoom);
        }
    }

    /**
     * 멤버의 채팅방 리스트 조회
     */
    public List<ChatRoom> getChatRoomsByMemberId(String memberId) {
        return chatRoomRepository.findByMemberIdSorted(memberId);
    }

    /**
     * 멤버 ID와 채팅방 ID에 대한 채팅방 조회
     */
    public ChatRoom getChatRoomByIdAndMemberId(Long chatroomId, String memberId) {
        return chatRoomRepository.findByIdAndMemberId(chatroomId, memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "해당 채팅방을 찾을 수 없습니다."));
    }

    /**
     * 채팅방 삭제
     */
    @Transactional
    public void deleteChatRoomAndMessages(Long chatroomId, String memberId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndMemberId(chatroomId, memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "해당 채팅방을 찾을 수 없습니다."));

        chatMessageService.deleteMessagesByChatRoom(chatRoom);
        chatRoomRepository.delete(chatRoom);
    }

    /**
     * 채팅방 즐겨찾기 설정 또는 해제
     */
    @Transactional
    public void updateFavoriteStatus(String memberId, Long chatRoomId, boolean favorite) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndMemberId(chatRoomId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "채팅방을 찾을 수 없습니다."));

        chatRoom.setFavorite(favorite);
        chatRoom.setFavoriteAddedAt(favorite ? LocalDateTime.now() : null);
        chatRoomRepository.save(chatRoom);
    }

    /**
     * FastAPI용 멀티파트 요청 생성
     */
    public MultiValueMap<String, Object> prepareMultipartRequest(String query, Long chatroomId, String memberId,
                                                                 Long personaId, List<MultipartFile> files, String modelType) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("query", query);
        body.add("chat_room_id", chatroomId);
        body.add("member_id", memberId);
        body.add("persona_id", personaId);
        body.add("model_type", modelType);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String filename = file.getOriginalFilename();
                if (filename == null)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일 이름이 없습니다.");

                String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
                List<String> allowedExtensions = List.of("pdf", "docx", "hwp", "txt");

                if (!allowedExtensions.contains(extension)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "지원하지 않는 파일 형식입니다. 허용된 형식: " + String.join(", ", allowedExtensions));
                }

                body.add("files", new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }
                });
            }
        }
        return body;
    }
}
