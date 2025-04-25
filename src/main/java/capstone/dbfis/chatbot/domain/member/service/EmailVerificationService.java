package capstone.dbfis.chatbot.domain.member.service;

import capstone.dbfis.chatbot.domain.member.dto.PasswordResetToken;
import capstone.dbfis.chatbot.domain.member.entity.EmailVerification;
import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.EmailVerificationRepository;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.domain.member.repository.PasswordResetTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;
    private final JavaMailSender javaMailSender;
    private static final String senderEmail = "trendb37@gmail.com";

    // 회원가입 인증 이메일 전송 메서드
    @Transactional
    public void sendSignUpVerificationEmail(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 랜덤하게 6자리 인증번호 생성
        Random random = new Random();
        String verificationCode = String.format("%06d", random.nextInt(1000000));


        emailVerificationRepository.findByMemberId(member.getId())
                .ifPresent(emailVerificationRepository::delete);

        EmailVerification emailVerification = EmailVerification.builder()
                .member(member)
                .verificationCode(verificationCode)
                .build();

        // 인증번호 저장
        emailVerificationRepository.save(emailVerification);

        // 회원가입 이메일 전송
        sendSignUpMail(member.getEmail(), verificationCode);
    }

    // 회원가입 이메일 전송 메서드
    public void sendSignUpMail(String email, String verificationCode) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("[TRENDB] 회원가입 이메일 인증");
            String body = "<h3>TRENDB 회원가입을 위한 인증 번호입니다.</h3>" +
                    "<h1>[" + verificationCode + "]</h1>" +
                    "<h3>감사합니다.</h3>";
            message.setText(body, "UTF-8", "html");
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
        javaMailSender.send(message);
    }

    // 회원가입 인증 코드 검증 메서드
    @Transactional
    public boolean verifySignUpCode(String code) {
        EmailVerification verification = emailVerificationRepository.findByVerificationCode(code)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 인증 코드입니다."));

        if (verification.isVerified()) {
            return false; // 이미 인증된 코드
        }

        verification.setVerified(true);
        Member member = verification.getMember();
        member.setVerified(true);
        memberRepository.save(member);

        return true;
    }

    // 비밀번호 재설정 이메일 전송
    @Transactional
    public void sendPasswordResetEmail(String email, String resetLink) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("[TRENDB] 비밀번호 재설정");

            String body = "<h3>TRENDB 비밀번호를 재설정하려면 아래 버튼을 클릭하세요.</h3>" +
                    "<p>아래 링크를 클릭하면 비밀번호를 변경할 수 있습니다.</p>" +
                    "<p><a href=\"" + resetLink + "\" style=\"display:inline-block; padding:10px 20px; background-color:#007bff; color:#ffffff; text-decoration:none; border-radius:5px;\">비밀번호 재설정하기</a></p>" +
                    "<p style=\"color: red; font-weight: bold;\">※ 해당 링크는 30분 동안만 유효합니다.</p>" +
                    "<p>감사합니다.</p>";

            message.setText(body, "UTF-8", "html");
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
        javaMailSender.send(message);
    }
}