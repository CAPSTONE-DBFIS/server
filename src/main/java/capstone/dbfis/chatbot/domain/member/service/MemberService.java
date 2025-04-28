package capstone.dbfis.chatbot.domain.member.service;

import capstone.dbfis.chatbot.domain.member.dto.*;
import capstone.dbfis.chatbot.domain.member.entity.EmailVerification;
import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.EmailVerificationRepository;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.domain.member.repository.PasswordResetTokenRepository;
import capstone.dbfis.chatbot.domain.team.service.TeamService;
import capstone.dbfis.chatbot.domain.token.service.RefreshTokenService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * 회원 관련 핵심 비즈니스 로직을 처리하는 서비스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final TeamService teamService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * 신규 회원가입 처리 후 인증 이메일을 전송합니다
     */
    @Transactional
    public void registerMember(AddMemberRequest req) {
        // 중복된 회원 정보 확인
        validateDuplicate(req);

        // 신규 회원 엔티티 생성 및 저장
        Member newMember = saveNewMember(req);

        // 이메일 인증 메일 전송
        sendNewSignUpEmail(newMember.getId());
    }

    /**
     * 사용자 로그인 정보를 검증하고 JWT 토큰을 발급합니다
     */
    @Transactional
    public LoginResponse login(LoginRequest req) {
        // 회원 조회 및 존재 여부 확인
        Member member = memberRepository.findById(req.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(req.getPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        }

        // 이메일 인증 완료 여부 확인
        if (!member.isVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일 인증이 필요합니다. 인증을 완료해주세요.");
        }

        // 액세스 및 리프레시 토큰 생성
        String accessToken = tokenProvider.generateToken(member, Duration.ofHours(2));
        String refreshToken = tokenProvider.generateToken(member, Duration.ofDays(14));
        refreshTokenService.updateOrSaveRefreshToken(member, refreshToken);

        // 토큰 응답 반환
        return new LoginResponse(accessToken, refreshToken);
    }

    /**
     * 인증 이메일을 재전송합니다
     */
    @Transactional
    public void sendNewSignUpEmail(String memberId) {
        // 회원 조회
        Member member = findMemberOrThrow(memberId);

        // 기존 인증 기록 삭제
        emailVerificationRepository.findByMemberId(memberId)
                .ifPresent(emailVerificationRepository::delete);

        // 인증 코드 생성 및 저장
        String verificationCode = generateVerificationCode();
        EmailVerification ev = EmailVerification.builder()
                .member(member)
                .verificationCode(verificationCode)
                .build();
        emailVerificationRepository.save(ev);

        // 인증 이메일 발송 시도
        try {
            sendSignUpMail(member.getEmail(), verificationCode);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "메일 발송 실패로 회원가입 인증을 완료할 수 없습니다.", e);
        }
    }

    /**
     * 이메일 인증 코드를 검증하고 인증 상태를 업데이트합니다
     */
    @Transactional
    public boolean verifySignUpCode(String memberId, String inputCode) {
        // 저장된 인증 기록 조회
        EmailVerification verification = emailVerificationRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "인증 요청이 존재하지 않습니다."));

        // 코드 일치 여부 확인
        if (!verification.getVerificationCode().equals(inputCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 인증 코드입니다.");
        }

        // 회원 인증 상태 업데이트 및 기록 삭제
        Member member = verification.getMember();
        member.setVerified(true);
        memberRepository.save(member);
        emailVerificationRepository.delete(verification);
        return true;
    }

    /**
     * 이메일로 회원 아이디를 조회하고 일부 마스킹하여 반환합니다
     */
    public String findId(String email) {
        // 이메일 기반 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 이메일을 가진 사용자가 없습니다."));

        // 아이디 마스킹 후 반환
        return maskId(member.getId());
    }

    /**
     * 비밀번호 재설정 링크를 이메일로 발송합니다
     */
    @Transactional
    public void findPassword(String email) {
        // 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 이메일을 가진 사용자가 없습니다."));

        // 기존 토큰 삭제
        passwordResetTokenRepository.deleteByMemberId(member.getId());
        passwordResetTokenRepository.flush();

        // 새 토큰 생성 및 저장
        String token = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(new PasswordResetToken(token, member));

        // 재설정 링크 전송
        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        sendPasswordResetEmail(member.getEmail(), resetLink);
    }

    /**
     * 비밀번호 재설정 토큰을 검증하고 비밀번호를 변경합니다
     */
    @Transactional
    public void resetPassword(PasswordResetRequest req) {
        // 토큰 조회
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(req.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다."));

        // 토큰 만료 여부 확인
        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호 재설정 토큰이 만료되었습니다.");
        }

        // 비밀번호 유효성 및 중복 확인
        Member member = resetToken.getMember();
        if (!isValidPassword(req.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다.");
        }

        if (passwordEncoder.matches(req.getNewPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이전과 동일한 비밀번호로 변경할 수 없습니다.");
        }

        // 새 비밀번호 저장 및 토큰 삭제
        member.setPassword(passwordEncoder.encode(req.getNewPassword()));
        memberRepository.save(member);
        passwordResetTokenRepository.delete(resetToken);
    }

    /**
     * 마이페이지 정보를 조회합니다
     */
    @Transactional(readOnly = true)
    public MyPageResponse getMyPageData(String memberId) {
        // 회원 조회
        Member member = findMemberOrThrow(memberId);

        // 마이페이지 응답 생성
        return MyPageResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .department(member.getDepartment())
                .role(member.getRole())
                .email(member.getEmail())
                .profileImage(member.getProfileImage())
                .teams(teamService.getUserTeams(memberId))
                .build();
    }

    /**
     * 프로필 정보를 업데이트합니다
     */
    @Transactional
    public void updateProfile(String id, UpdateProfileRequest req) {
        // 회원 조회
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 회원 ID입니다."));

        // 변경할 값 설정
        Member updated = member.toBuilder()
                .name(req.getName() != null ? req.getName() : member.getName())
                .nickname(req.getNickname() != null ? req.getNickname() : member.getNickname())
                .phone(req.getPhone() != null ? req.getPhone() : member.getPhone())
                .profileImage(req.getProfileImage() != null ? req.getProfileImage() : member.getProfileImage())
                .department(req.getDepartment() != null ? req.getDepartment() : member.getDepartment())
                .role(req.getRole() != null ? req.getRole() : member.getRole())
                .build();

        // 변경 내용 저장
        memberRepository.save(updated);
    }

    /**
     * 비밀번호를 변경합니다
     */
    @Transactional
    public void updatePassword(String id, UpdatePasswordRequest req) {
        // 회원 조회
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 회원 ID입니다."));
        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(req.getOldPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }
        // 이전 비밀번호와 중복 검증
        if (passwordEncoder.matches(req.getNewPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이전과 동일한 비밀번호로 변경할 수 없습니다.");
        }
        // 새 비밀번호 저장
        member.setPassword(passwordEncoder.encode(req.getNewPassword()));
        memberRepository.save(member);
    }

    /**
     * 회원 아이디 및 이메일 중복 여부를 검증합니다
     */
    private void validateDuplicate(AddMemberRequest req) {
        // ID 중복 체크
        if (memberRepository.findById(req.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 존재하는 ID입니다.");
        }
        // 이메일 중복 체크
        if (memberRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다.");
        }
    }

    /**
     * 신규 회원 정보를 저장합니다
     */
    private Member saveNewMember(AddMemberRequest req) {
        // 엔티티 생성 및 저장
        return memberRepository.save(Member.builder()
                .id(req.getId())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .nickname(req.getNickname())
                .department(req.getDepartment())
                .role(req.getRole())
                .build());
    }

    /**
     * 회원 조회 후 없으면 예외 발생
     */
    private Member findMemberOrThrow(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 ID로 가입된 회원이 존재하지 않습니다."));
    }

    /**
     * 인증 코드 생성을 위한 6자리 숫자를 생성합니다
     */
    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    /**
     * 회원가입 인증 이메일 발송
     */
    private void sendSignUpMail(String email, String verificationCode) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("[TRENDB] 회원가입 이메일 인증");
            String body = "<h3>인증 번호: [" + verificationCode + "]</h3>";
            message.setText(body, "UTF-8", "html");
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "메일 발송 실패", e);
        }
    }

    /**
     * 비밀번호 재설정 링크 이메일 발송
     */
    private void sendPasswordResetEmail(String email, String resetLink) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("[TRENDB] 비밀번호 재설정");
            String body = "<a href='" + resetLink + "'>비밀번호 재설정</a>";
            message.setText(body, "UTF-8", "html");
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "메일 발송 실패", e);
        }
    }

    /**
     * ID 일부를 마스킹
     */
    private String maskId(String memberId) {
        if (memberId.length() <= 3) return memberId;
        return memberId.substring(0, 3) + "*".repeat(memberId.length() - 3);
    }

    /**
     * 비밀번호 유효성 검사 (길이/특수문자)
     */
    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches(".*[!@#$%^&*].*");
    }

    /**
     * 비밀번호 재설정 토큰 유효성 확인
     */
    public boolean isValidPasswordResetToken(String token) {
        Optional<PasswordResetToken> resetToken = passwordResetTokenRepository.findByToken(token);
        return resetToken.isPresent() && !resetToken.get().isExpired();
    }
}