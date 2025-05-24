package capstone.dbfis.chatbot.domain.filesharing.repository;

import capstone.dbfis.chatbot.domain.filesharing.entity.File;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByTeamIdAndFolderId(Long teamId, Long folderId, Sort sort);
    List<File> findTop4ByTeamIdOrderByDownloadCountDesc(Long teamId);
    // 팀 ID로 사용량 합계 조회
    @Query("SELECT SUM(f.size) FROM File f WHERE f.teamId = :teamId")
    Long sumSizeByTeamId(@Param("teamId") Long teamId);

    // 파일명 키워드 검색 (영어 대소문자 상관 없이, 한글 검색 가능하도록)
    @Query(value = """
    SELECT *
      FROM files
     WHERE team_id = :teamId
       AND LOWER(original_name) LIKE LOWER(CONCAT('%', :kw, '%'))
    """, nativeQuery = true)
    List<File> searchByNameNative(
            @Param("teamId") Long teamId,
            @Param("kw")     String keyword
    );
}