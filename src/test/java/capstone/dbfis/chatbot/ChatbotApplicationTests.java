package capstone.dbfis.chatbot;

import capstone.dbfis.chatbot.domain.member.Member;
import capstone.dbfis.chatbot.domain.member.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ChatbotApplicationTests {
	@Autowired private MemberRepository memberRepository;
	@Test
	void saveUser() {
		// given
		Member member = Member.builder()
				.id("User1")
				.name("홍길동")
				.email("gildong@example.com")
				.phone("010-1234-5678")
				.nickname("gildong")
				.interests("Reading, Coding")
				.profileImage("default.png")
				.personaPreset(1)
				.refreshToken("dummy_refresh_token")
				.build();

		// when
		Member savedMember = memberRepository.save(member);

		// then
		assertThat(savedMember).isNotNull();
		assertThat(savedMember.getId()).isEqualTo("User1");
		assertThat(savedMember.getName()).isEqualTo("홍길동");
		assertThat(savedMember.getEmail()).isEqualTo("gildong@example.com");
	}

}
