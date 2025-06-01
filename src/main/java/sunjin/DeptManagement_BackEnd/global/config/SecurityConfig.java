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
import sunjin.DeptManagement_BackEnd.global.auth.handler.CustomAccessDeniedHandler;
import sunjin.DeptManagement_BackEnd.global.auth.service.JwtProvider;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/employee/**").hasAuthority("EMPLOYEE");
                    auth.requestMatchers("/teamleader/**").hasAuthority("TEAMLEADER");
                    auth.requestMatchers("/centerdirector/**").hasAuthority("CENTERDIRECTOR");
                    auth.requestMatchers("/**").permitAll();

                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(exceptionHandling -> {
                    exceptionHandling.accessDeniedHandler(accessDeniedHandler);
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
