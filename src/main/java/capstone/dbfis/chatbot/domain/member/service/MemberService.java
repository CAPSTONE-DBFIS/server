package capstone.dbfis.chatbot.domain.member.service;

import capstone.dbfis.chatbot.domain.member.dto.*;
import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.domain.member.repository.PasswordResetTokenRepository;
import capstone.dbfis.chatbot.domain.token.service.RefreshTokenService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final TokenProvider tokenProvider;

    @Transactional
    public void registerMember(AddMemberRequest req) {
        validateMember(req);
        Member newMember = memberRepository.save(Member.builder()
                .id(req.getId())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .nickname(req.getNickname())
                .interests(req.getInterests())
                .personaPreset(req.getPersona_preset() != null ? Integer.parseInt(req.getPersona_preset()) : 0)
                .department(req.getDepartment())
                .build());

        // 이메일 인증 코드 생성 및 발송
        emailVerificationService.sendSignUpVerificationEmail(newMember.getId());
    }

    private void validateMember(AddMemberRequest req) {
        if (memberRepository.findById(req.getId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 ID입니다.");
        }
        if (memberRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
    }

    // 로그인 & JWT 발급
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        Member member = memberRepository.findById(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 액세스 & 리프레시 토큰 발급
        String accessToken = tokenProvider.generateToken(member, Duration.ofHours(2));
        String refreshToken = tokenProvider.generateToken(member, Duration.ofDays(14));

        // 기존 리프레시 토큰이 있는지 확인 후 업데이트 or 저장
        refreshTokenService.updateOrSaveRefreshToken(member, refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }

    // 비밀번호 재설정 요청 처리 (토큰 생성 및 비밀번호 재설정 이메일 전송)
    @Transactional
    public void requestPasswordReset(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일을 가진 사용자가 없습니다."));

        // 랜덤한 토큰 생성 (UUID 사용)
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, member);

        // 기존 토큰 삭제 후 새 토큰 저장
        passwordResetTokenRepository.deleteByMemberId(member.getId());
        passwordResetTokenRepository.flush();
        passwordResetTokenRepository.save(resetToken);

        // 비밀번호 재설정 링크 생성 (배포시 수정 필요)
        String resetLink = "http://localhost:8080/reset-password?token=" + token;

        // 이메일 전송
        emailVerificationService.sendPasswordResetEmail(member.getEmail(), resetLink);
    }

    // 비밀번호 재설정 (토큰 검증 후 비밀번호 변경)
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        // 토큰 찾기
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다."));

        // 토큰 만료 검증
        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken); // 만료된 토큰 삭제
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호 재설정 토큰이 만료되었습니다. 다시 요청해주세요.");
        }

        Member member = resetToken.getMember();
        // 비밀번호 형식 체크 (8자 이상, 특수문자 포함 등)
        if (!isValidPassword(request.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다.");
        }

        // 이전과 동일한 비밀번호인지 검증
        if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이전과 동일한 비밀번호로 변경할 수 없습니다.");
        }

        // 비밀번호 변경
        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);

        // 비밀번호 재설정 토큰 삭제
        passwordResetTokenRepository.delete(resetToken);
    }

    // 비밀번호 변경 토큰이 유효한지 확인하는 메서드
    public boolean isValidPasswordResetToken(String token) {
        Optional<PasswordResetToken> resetToken = passwordResetTokenRepository.findByToken(token);
        return resetToken.isPresent() && !resetToken.get().isExpired(); // 토큰이 존재하고 만료되지 않았는지 확인
    }

    // 비밀번호 형식 검증 메서드
    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches(".*[!@#$%^&*].*");
    }

    @Transactional
    public Member updateMember(String id, UpdateMemberRequest req) {
        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));

        existingMember.setName(req.getName());
        existingMember.setPhone(req.getPhone());
        existingMember.setNickname(req.getNickname());
        existingMember.setInterests(req.getInterests());
        existingMember.setDepartment(req.getDepartment());
        existingMember.setPersonaPreset(req.getPersona_preset() != null ? Integer.parseInt(req.getPersona_preset()) : existingMember.getPersonaPreset());
        if (req.getPassword() != null) {
            existingMember.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        return memberRepository.save(existingMember);
    }

    @Transactional
    public void deleteMember(String id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("invalid member ID"));
        memberRepository.delete(member);
    }

    public Member findById(String id) {
        return memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("invalid member ID"));
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("invalid email"));
    }

    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }
}