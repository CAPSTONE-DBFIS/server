package capstone.dbfis.chatbot.global.config.springsecurity;

import capstone.dbfis.chatbot.domain.member.service.MemberService;
import capstone.dbfis.chatbot.domain.token.service.RefreshTokenService;
import capstone.dbfis.chatbot.global.config.jwt.JwtSuccessHandler;
import capstone.dbfis.chatbot.global.config.jwt.TokenAuthenticationFilter;
import capstone.dbfis.chatbot.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtSuccessHandler jwtSuccessHandler;

    // 스프링 시큐리티 기능 비활성화
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/static/**"); // 정적 리소스에 대한 접근 제외
    }

    // 특정 http 요청에 대한 웹 기반 보안 구성
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF 비활성화
        http.csrf().disable()
                .httpBasic().disable()
                .formLogin()
                .loginPage("/login")
                .successHandler(jwtSuccessHandler) // 성공 핸들러 설정
                .and()
                .authorizeRequests()
                .requestMatchers("/login").permitAll() // 회원가입 페이지는 인증 없이 접근 가능
                .requestMatchers("/signup").permitAll() // 회원가입 페이지는 인증 없이 접근 가능
                .requestMatchers("/api/token").permitAll() // 토큰 재발급 url 인증 없이 접근 가능
                .requestMatchers("/api/**").authenticated() // 나머지 api url은 인증 필요
                .anyRequest().permitAll(); // 그 외의 요청은 모두 허용

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션 비활성화

        // 커스텀 필터 추가
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // 로그아웃 설정
        http.logout()
                .logoutSuccessUrl("/");

        // /api url에 대한 401 에러 코드 예외 처리
        http.exceptionHandling()
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**"));

        return http.build();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    // AuthenticationManager 설정
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}