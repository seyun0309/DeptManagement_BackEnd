package sunjin.DeptManagement_BackEnd.global.auth.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sunjin.DeptManagement_BackEnd.global.auth.dto.SecurityMemberDTO;
import sunjin.DeptManagement_BackEnd.global.auth.service.JwtProvider;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null) {
            String[] authElements = header.split(" ");
            if (authElements.length == 2 && "Bearer".equals(authElements[0])) {
                try {
                    Claims claims = jwtProvider.verifyToken(authElements[1]);
                    SecurityMemberDTO securityMemberDTO = SecurityMemberDTO.fromClaims(claims);
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(securityMemberDTO, null, List.of(new SimpleGrantedAuthority(securityMemberDTO.getRole().name())));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } catch (Exception e) {
                    SecurityContextHolder.clearContext();
                    request.setAttribute("exception", e.getMessage());
                }
            }
        }

        doFilter(request, response, filterChain);
    }
}