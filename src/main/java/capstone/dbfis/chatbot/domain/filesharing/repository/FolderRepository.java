package capstone.dbfis.chatbot.domain.filesharing.repository;

import capstone.dbfis.chatbot.domain.filesharing.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByTeamIdAndParentId(Long teamId, Long parentId);
    List<Folder> findByTeamIdAndParentIsNull(Long teamId);
    List<Folder> findByParentId(Long parentId);
}