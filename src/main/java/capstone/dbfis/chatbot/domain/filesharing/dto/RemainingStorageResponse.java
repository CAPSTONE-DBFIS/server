package capstone.dbfis.chatbot.domain.filesharing.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RemainingStorageResponse {
    private long limitMegaBytes;     // 총 한도 (1GB)
    private long usedMegaBytes;      // 현재 사용량(MB)
    private long remainingMegaBytes; // 잔여 용량(MB)
}
