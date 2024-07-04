package sunjin.DeptManagement_BackEnd.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sunjin.DeptManagement_BackEnd.global.auth.filter.JwtAuthFilter;
import sunjin.DeptManagement_BackEnd.global.auth.service.JwtProvider;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProvider jwtProvider;

    private static final String EMPLOYEE = "EMPLOYEE";
    private static final String ADMIN = "ADMIN";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/auth/login", "auth/signup").permitAll();
                    auth.requestMatchers("/**").permitAll();

//                    auth.requestMatchers(HttpMethod.POST, "/put_your_endpoint").hasAuthority(STUDENT_COUNCIL_MANAGER);
//                    auth.requestMatchers(HttpMethod.GET, "/put_your_endpoint").hasAuthority(STUDENT_COUNCIL_MANAGER);
//                    auth.requestMatchers(HttpMethod.DELETE, "/put_your_endpoint").hasAuthority(STUDENT_COUNCIL_MANAGER);
//                    auth.requestMatchers(HttpMethod.PATCH, "/put_your_endpoint").hasAuthority(STUDENT_COUNCIL_MANAGER);
//
//                    auth.requestMatchers(HttpMethod.POST, "/put_your_endpoint").hasAuthority(CLUB_MANAGER);
//                    auth.requestMatchers(HttpMethod.GET, "/put_your_endpoint").hasAuthority(CLUB_MANAGER);
//                    auth.requestMatchers(HttpMethod.DELETE, "/put_your_endpoint").hasAuthority(CLUB_MANAGER);
//                    auth.requestMatchers(HttpMethod.PATCH, "/put_your_endpoint").hasAuthority(CLUB_MANAGER);
//
//                    auth.requestMatchers(HttpMethod.POST, "/put_your_endpoint").hasAuthority(GENERAL_STUDENT_COUNCIL);
//                    auth.requestMatchers(HttpMethod.GET, "/put_your_endpoint").hasAuthority(GENERAL_STUDENT_COUNCIL);
//                    auth.requestMatchers(HttpMethod.DELETE, "/put_your_endpoint").hasAuthority(GENERAL_STUDENT_COUNCIL);
//                    auth.requestMatchers(HttpMethod.PATCH, "/put_your_endpoint").hasAuthority(GENERAL_STUDENT_COUNCIL);
//
//                    auth.requestMatchers(HttpMethod.POST, "/put_your_endpoint").hasAuthority(STUDENT);
//                    auth.requestMatchers(HttpMethod.GET, "/put_your_endpoint").hasAuthority(STUDENT);
//                    auth.requestMatchers(HttpMethod.DELETE, "/put_your_endpoint").hasAuthority(STUDENT);
//                    auth.requestMatchers(HttpMethod.PATCH, "/put_your_endpoint").hasAuthority(STUDENT);
//
//                    auth.requestMatchers(HttpMethod.POST, "/put_your_endpoint").hasAuthority(ADMIN);
//                    auth.requestMatchers(HttpMethod.GET, "/put_your_endpoint").hasAuthority(ADMIN);
//                    auth.requestMatchers(HttpMethod.DELETE, "/put_your_endpoint").hasAuthority(ADMIN);
//                    auth.requestMatchers(HttpMethod.PATCH, "/put_your_endpoint").hasAuthority(ADMIN);
                    //todo
                    //권한별로 엔드포인트 설정하기
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .logout(Customizer.withDefaults());

        return httpSecurity.build();

    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
