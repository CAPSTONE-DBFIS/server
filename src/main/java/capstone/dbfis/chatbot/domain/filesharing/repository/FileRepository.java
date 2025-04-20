package capstone.dbfis.chatbot.domain.filesharing.repository;

import capstone.dbfis.chatbot.domain.filesharing.entity.File;
import org.apache.http.entity.FileEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByTeamIdAndFolderId(Long teamId, Long folderId, Sort sort);
    List<File> findTop4ByTeamIdOrderByDownloadCountDesc(Long teamId);
}