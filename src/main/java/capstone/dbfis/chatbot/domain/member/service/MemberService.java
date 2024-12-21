package capstone.dbfis.chatbot.domain.member.service;

import capstone.dbfis.chatbot.domain.member.entity.Member;
import capstone.dbfis.chatbot.domain.member.repository.MemberRepository;
import capstone.dbfis.chatbot.domain.member.dto.AddMemberRequest;
import capstone.dbfis.chatbot.domain.member.dto.UpdateMemberRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public String registerMember(AddMemberRequest req) {
        validateMember(req);
        Member newMember = memberRepository.save(Member.builder()
                .id(req.getId())
                .password(bCryptPasswordEncoder.encode(req.getPassword()))
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
            existingMember.setPassword(bCryptPasswordEncoder.encode(req.getPassword()));
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