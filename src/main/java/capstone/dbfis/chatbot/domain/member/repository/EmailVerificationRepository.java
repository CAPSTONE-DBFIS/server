package capstone.dbfis.chatbot.domain.member.repository;

import capstone.dbfis.chatbot.domain.member.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByMemberId(String memberId); // 회원 ID로 인증 정보 검색
    Optional<EmailVerification> findByMemberIdAndVerificationCode(String memberId, String code); // 회원 ID와 인증번호로 검색
}
