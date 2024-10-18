package uni.backend.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import uni.backend.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable);
/*        http
                .exceptionHandling((exception)
                -> exception.authenticationEntryPoint(new CustomAuthenticationEntryPoint()));*/
        // 인증되지 않은 사용자가 리소스에 접근했을 때 수행되는 핸들러

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/new", "/login").permitAll()  // 회원가입 및 로그인 페이지는 인증 없이 접근 가능
                        .anyRequest().authenticated())  // 나머지 요청은 인증 필요
                .formLogin(form -> form
                        .loginPage("/login")  // 로그인 페이지 URL 설정
                        .loginProcessingUrl("/login")  // 로그인 폼 action URL
                        .defaultSuccessUrl("/", true)  // 로그인 성공 시 이동할 페이지
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
