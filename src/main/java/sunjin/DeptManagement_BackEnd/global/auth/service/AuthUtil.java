package sunjin.DeptManagement_BackEnd.global.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

@Component
@RequiredArgsConstructor
public class AuthUtil {
    private final JwtProvider jwtProvider;
    private final RedisUtil redisUtil;
    private final MemberRepository memberRepository;

    public Member extractMemberAfterTokenValidation() {
        String token = jwtProvider.extractIdFromTokenInHeader();

        // 1. JWT 블랙리스트 검사
        String status = redisUtil.getData(token);
        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN); // 등록되지 않은 토큰 (ex. 위조/만료/비정상 발급 등)
        }

        if ("logout".equals(status)) {
            throw new BusinessException(ErrorCode.LOGGED_OUT_ACCESS_TOKEN); //리프래시 토큰을 사용해서 액세스 토큰 다시 발급받기
        }

        // 2. JWT 유효성 검사
        jwtProvider.verifyToken(token);

        // 3. 토큰 통해서 Member Id 반환
        Long memberId = jwtProvider.extractIdFromToken(token);

        // 4. memberId 유효성 검사 후 Member 리턴
        return memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public String extractTokenAfterTokenValidation() {
        String token = jwtProvider.extractIdFromTokenInHeader();

        // 1. JWT 블랙리스트 검사
        String status = redisUtil.getData(token);
        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN); // 등록되지 않은 토큰 (ex. 위조/만료/비정상 발급 등)
        }

        if ("logout".equals(status)) {
            throw new BusinessException(ErrorCode.LOGGED_OUT_ACCESS_TOKEN); //리프래시 토큰을 사용해서 액세스 토큰 다시 발급받기
        }

        // 2. JWT 유효성 검사
        jwtProvider.verifyToken(token);

        // 3. 토큰 반환
        return token;
    }
}
