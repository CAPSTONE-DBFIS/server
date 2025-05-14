package capstone.dbfis.chatbot.domain.chatbot.service;

import capstone.dbfis.chatbot.domain.chatbot.dto.ChatDashboardDto;
import capstone.dbfis.chatbot.domain.chatbot.dto.ChatRoomDto;
import capstone.dbfis.chatbot.domain.chatbot.dto.TeamDto;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoom;
import capstone.dbfis.chatbot.domain.chatbot.entity.ChatRoomType;
import capstone.dbfis.chatbot.domain.chatbot.repository.ChatRoomRepository;
import capstone.dbfis.chatbot.domain.member.dto.MyPageResponse;
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
        if (type == ChatRoomType.TEAM) {
            if (teamId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teamId는 필수입니다.");

            if (!teamService.isUserInTeam(teamId, memberId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 팀에 속해있지 않습니다.");
            }

            ChatRoom room = ChatRoom.builder()
                    .name("새 팀 채팅방")
                    .type(ChatRoomType.TEAM)
                    .teamId(teamId)
                    .memberId(memberId)
                    .createdAt(LocalDateTime.now())
                    .build();

            ChatRoom saved = chatRoomRepository.save(room);
            return ChatRoomDto.builder()
                    .id(saved.getId()).name(saved.getName()).type(saved.getType()).teamId(saved.getTeamId())
                    .favorite(saved.isFavorite()).favoriteAddedat(saved.getFavoriteAddedAt()).build();
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .memberId(memberId)
                .name("새 채팅방")
                .type(ChatRoomType.PERSONAL)
                .createdAt(LocalDateTime.now())
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);
        return ChatRoomDto.builder()
                .id(chatRoom.getId()).name(chatRoom.getName()).type(chatRoom.getType())
                .teamId(chatRoom.getTeamId()).favorite(chatRoom.isFavorite())
                .favoriteAddedat(chatRoom.getFavoriteAddedAt()).build();
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
        // 개인 채팅방만 조회
        List<ChatRoom> personalRooms = chatRoomRepository.findByMemberIdSorted(memberId)
                .stream()
                .filter(room -> room.getType() == ChatRoomType.PERSONAL)
                .toList();

        List<Long> teamIds = teamService.getUserTeams(memberId).stream()
                .map(MyPageResponse.TeamResponse::getTeamId)
                .toList();

        // 팀 채팅방만 조회
        List<ChatRoom> teamRooms = teamIds.isEmpty() ? List.of()
                : chatRoomRepository.findByTeamIdInAndType(teamIds, ChatRoomType.TEAM);

        List<ChatRoom> all = new ArrayList<>();
        all.addAll(personalRooms);
        all.addAll(teamRooms);
        return all;
    }

    /**
     * 멤버 ID와 채팅방 ID에 대한 채팅방 조회
     */
    public ChatRoom getChatRoomByIdAndMemberId(Long chatroomId, String memberId) {
        ChatRoom room = chatRoomRepository.findById(chatroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "채팅방 없음"));

        if (room.getTeamId() != null) {
            if (!teamService.isUserInTeam(room.getTeamId(), memberId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "팀 채팅방 접근 불가");
            }
        } else {
            if (!room.getMemberId().equals(memberId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "개인 채팅방 접근 불가");
            }
        }

        return room;
    }

    /**
     * 채팅방 삭제
     */
    @Transactional
    public void deleteChatRoomAndMessages(Long chatroomId, String memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatroomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "채팅방을 찾을 수 없습니다."));

        // 팀 채팅방일 경우, 해당 팀의 멤버인지 확인
        if (chatRoom.getType() == ChatRoomType.TEAM) {
            if (!teamService.isUserInTeam(chatRoom.getTeamId(), memberId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 팀 채팅방을 삭제할 수 없습니다.");
            }
        } else {
            // 개인 채팅방은 본인만 삭제 가능
            if (!chatRoom.getMemberId().equals(memberId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 채팅방을 삭제할 수 없습니다.");
            }
        }

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
