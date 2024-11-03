package capstone.dbfis.chatbot.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional
    public Member registerMember(Member member) {
        validateMember(member);
        return memberRepository.save(member);
    }

    private void validateMember(Member member) {
        if (memberRepository.findById(member.getId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 ID입니다.");
        }
    }

    public Member findById(String id) {
        return memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("invalid member"));
    }


}
