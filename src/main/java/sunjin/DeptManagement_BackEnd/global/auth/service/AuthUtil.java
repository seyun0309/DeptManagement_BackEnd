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

        // 1. 헤더에서 토큰 추출
        String token = jwtProvider.extractIdFromTokenInHeader();

        // 2. 액세스 토큰 블랙리스트 검사
        String status = redisUtil.getData(token);
        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN); // 등록되지 않은 토큰 (ex. 위조/만료/비정상 발급 등)
        }

        // 3. 액세스 토큰이 logout인 경우 토큰 재발급
        if ("logout".equals(status)) {
            throw new BusinessException(ErrorCode.LOGGED_OUT_ACCESS_TOKEN); //리프래시 토큰을 사용해서 액세스 토큰 다시 발급받기
        }

        // 4. 토큰 유효성 검사
        jwtProvider.verifyToken(token);

        // 5. 토큰을 통해 Member 고유 ID 추출
        Long memberId = jwtProvider.extractIdFromToken(token);

        // 4. memberId를 통해 Member 추출 후 반환
        return memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public String extractTokenAfterTokenValidation() {

        // 1. 헤더에서 토큰 추출
        String token = jwtProvider.extractIdFromTokenInHeader();

        // 2. 액세스 토큰 블랙리스트 검사
        String status = redisUtil.getData(token);
        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN); // 등록되지 않은 토큰 (ex. 위조/만료/비정상 발급 등)
        }

        // 3. 액세스 토큰이 logout인 경우 토큰 재발급
        if ("logout".equals(status)) {
            throw new BusinessException(ErrorCode.LOGGED_OUT_ACCESS_TOKEN); //리프래시 토큰을 사용해서 액세스 토큰 다시 발급받기
        }

        // 4. 토큰 유효성 검사
        jwtProvider.verifyToken(token);

        // 5. 정상 토큰 반환
        return token;
    }
}
