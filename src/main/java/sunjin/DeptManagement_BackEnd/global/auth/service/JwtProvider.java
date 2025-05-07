package sunjin.DeptManagement_BackEnd.global.auth.service;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.global.auth.dto.GeneratedTokenDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.SecurityMemberDTO;
import sunjin.DeptManagement_BackEnd.global.config.JwtProperties;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.enums.Role;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import static sunjin.DeptManagement_BackEnd.global.enums.ErrorCode.MEMBER_NOT_FOUND;
import static sunjin.DeptManagement_BackEnd.global.enums.ErrorCode.MISMATCH_REFRESH_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtConfig;
    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;

    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private final HttpServletRequest request;

    @Getter
    private Key signingKey;
    private JwtParser jwtParser;

    private static final Long ACCESS_TOKEN_PERIOD = 1000L * 60L * 60L; // 1시간
    private static final Long REFRESH_TOKEN_PERIOD = 1000L * 60L * 60L * 24L * 14L; // 2주

    @PostConstruct
    protected void init() {
        String secretKey = Base64.getEncoder().encodeToString(jwtConfig.getSecret().getBytes());
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);
        signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        jwtParser = Jwts.parserBuilder().setSigningKey(signingKey).build();
    }

    @Transactional
    public GeneratedTokenDTO generateTokens(SecurityMemberDTO securityMemberDTO) {
        String accessToken = generateToken(securityMemberDTO, ACCESS_TOKEN_PERIOD);
        String refreshToken = generateToken(securityMemberDTO, REFRESH_TOKEN_PERIOD);
        String userName = securityMemberDTO.getUserName();
        Role role = securityMemberDTO.getRole();

        redisUtil.setDataExpire(accessToken, "login", ACCESS_TOKEN_PERIOD);

        saveRefreshToken(securityMemberDTO.getId(), refreshToken);

        return GeneratedTokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userName(userName)
                .role(role)
                .build();
    }

    private String generateToken(SecurityMemberDTO securityMemberDTO, Long tokenPeriod) {
        Claims claims = Jwts.claims().setSubject("id");
        claims.put("loginId", securityMemberDTO.getLoginId());
        claims.put("role", securityMemberDTO.getRole().name());
        claims.setId(String.valueOf(securityMemberDTO.getId()));
        Date now = new Date();

        return Jwts.builder().setClaims(claims).setIssuedAt(now).setExpiration(new Date(now.getTime() + tokenPeriod)).signWith(signingKey, signatureAlgorithm).compact();
    }

    @Transactional
    public GeneratedTokenDTO reissueToken(String refreshToken) {
        Claims claims;

        try {
            claims = verifyToken(refreshToken);
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN); // 로그인으로 이동
        }

        SecurityMemberDTO securityMemberDTO = SecurityMemberDTO.fromClaims(claims);

        Member member = memberRepository.findById(securityMemberDTO.getId())
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        if (!refreshToken.equals(member.getRefreshToken())) {
            throw new BusinessException(MISMATCH_REFRESH_TOKEN);
        }

        String reissuedAccessToken = generateToken(securityMemberDTO, ACCESS_TOKEN_PERIOD);
        String reissuedRefreshToken = generateToken(securityMemberDTO, REFRESH_TOKEN_PERIOD);

        // 새로운 토큰 저장
        member.setRefreshToken(reissuedRefreshToken);
        memberRepository.save(member);

        // Redis 등록
        redisUtil.setDataExpire(reissuedAccessToken, "login", ACCESS_TOKEN_PERIOD);

        return GeneratedTokenDTO.builder()
                .accessToken(reissuedAccessToken)
                .refreshToken(reissuedRefreshToken)
                .userName(securityMemberDTO.getUserName())
                .role(securityMemberDTO.getRole())
                .build();
    }

    public Claims verifyToken(String token) {
        try {
            return jwtParser.parseClaimsJws(token).getBody();
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new JwtException("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            throw new JwtException("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new JwtException("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new JwtException("잘못된 JWT 토큰입니다.");
        }
    }

    public long getRemainingExpiration(String token) {
        Date expiration = Jwts.parser()
                .setSigningKey(token)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        return expiration.getTime() - System.currentTimeMillis();
    }

    public String extractIdFromTokenInHeader() {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        } else {
            throw new IllegalArgumentException("Token not found in header.");
        }
    }

    public long extractIdFromToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(signingKey).parseClaimsJws(token);
            String idString = claims.getBody().get("jti", String.class);
            return Long.parseLong(idString);
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Error extracting ID from token.");
        }
    }

    private void saveRefreshToken(Long id, String refreshToken) {
        Optional<Member> findMember = memberRepository.findById(id);
        findMember.ifPresent(member -> memberRepository.updateRefreshToken(member.getId(), refreshToken));
    }
}
