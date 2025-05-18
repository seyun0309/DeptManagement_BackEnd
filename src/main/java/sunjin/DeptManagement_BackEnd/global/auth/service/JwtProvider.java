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
import sunjin.DeptManagement_BackEnd.global.auth.dto.response.ReissuedTokenResponseDTO;
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

        redisUtil.deleteData(securityMemberDTO.getLoginId());
        redisUtil.setDataExpire(securityMemberDTO.getLoginId(), refreshToken, REFRESH_TOKEN_PERIOD);

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
    public ReissuedTokenResponseDTO reissueToken(String refreshToken) {
        // 리프레시 토큰 통해서 사용자 로그인 아이디 추출
        String loginId = getLoginIdFromToken(refreshToken);

        // 로그인 아이디 통해서 레디스에 저장되어 있는 사용자 리프레시 토큰 추출
        String redisToken = redisUtil.getData(loginId);

        // 헤더로 받은 리프레시 토큰 | 레디스에 있는 리프레시 토큰 비교
        if (!refreshToken.equals(redisToken)) { // 토큰 유효성(탈취) 검사
            redisUtil.deleteData(loginId); // 불일치시 해당 사용자 리프레시 토큰 레디스에서 삭제
            throw new BusinessException(ErrorCode.TOKEN_REISSUE_FORBIDDEN); // 재로그인 유도
        }

        // 리프레시 토큰 유효성 검사
        Claims claims = verifyToken(refreshToken);

        SecurityMemberDTO securityMemberDTO = SecurityMemberDTO.fromClaims(claims);

        Member member = memberRepository.findById(securityMemberDTO.getId())
                .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        if (!refreshToken.equals(member.getRefreshToken())) {
            throw new BusinessException(MISMATCH_REFRESH_TOKEN);
        }

        // 액세스 토큰, 리프레시 토큰 재발급
        String reissuedAccessToken = generateToken(securityMemberDTO, ACCESS_TOKEN_PERIOD);
        String reissuedRefreshToken = generateToken(securityMemberDTO, REFRESH_TOKEN_PERIOD);

        // 새로운 토큰 저장
        member.setRefreshToken(reissuedRefreshToken);
        memberRepository.save(member);

        redisUtil.deleteData(loginId);

        // Redis 등록
        redisUtil.setDataExpire(reissuedAccessToken, "login", ACCESS_TOKEN_PERIOD);
        redisUtil.setDataExpire(securityMemberDTO.getLoginId(), reissuedRefreshToken, REFRESH_TOKEN_PERIOD);

        return ReissuedTokenResponseDTO.builder()
                .accessToken(reissuedAccessToken)
                .refreshToken(reissuedRefreshToken)
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
                .setSigningKey(signingKey)
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

    public String getLoginIdFromToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(signingKey).parseClaimsJws(token);
            return claims.getBody().get("loginId", String.class); // 커스텀 클레임에서 꺼냄
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Error extracting loginId from token.");
        }
    }

    private void saveRefreshToken(Long id, String refreshToken) {
        Optional<Member> findMember = memberRepository.findById(id);
        findMember.ifPresent(member -> memberRepository.updateRefreshToken(member.getId(), refreshToken));
    }
}
