package capstone.dbfis.chatbot.domain.member.repository;

import capstone.dbfis.chatbot.domain.member.dto.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByMemberId(String memberId);
}
