package capstone.dbfis.chatbot.domain.member.service;

import capstone.dbfis.chatbot.domain.member.dto.LoginRequest;
import capstone.dbfis.chatbot.domain.member.dto.LoginResponse;
import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.domain.member.dto.AddMemberRequest;
import capstone.dbfis.chatbot.domain.member.dto.UpdateMemberRequest;
import capstone.dbfis.chatbot.domain.token.entity.RefreshToken;
import capstone.dbfis.chatbot.domain.token.service.RefreshTokenService;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final TokenProvider tokenProvider;

    @Transactional
    public String registerMember(AddMemberRequest req) {
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
        emailVerificationService.sendVerificationEmail(newMember.getId());

        return newMember.getId();
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
    public LoginResponse authenticate(LoginRequest request) {
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
        Optional<RefreshToken> existingToken = refreshTokenService.findByMemberId(member.getId());
        if (existingToken.isPresent()) {
            refreshTokenService.updateRefreshToken(member, refreshToken);
        } else {
            refreshTokenService.saveRefreshToken(member, refreshToken);
        }

        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public void findPassWord(UpdateMemberRequest req) {
        Member existingMember = memberRepository.findById(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email"));

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