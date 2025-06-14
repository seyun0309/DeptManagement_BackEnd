package sunjin.DeptManagement_BackEnd.global.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.global.auth.dto.GeneratedTokenDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.SecurityMemberDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.request.LoginRequestDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.request.SignUpRequestDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.response.SignUpResponseDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.response.VerifyResponseDTO;
import sunjin.DeptManagement_BackEnd.global.enums.DeptType;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.util.Optional;

import static sunjin.DeptManagement_BackEnd.global.enums.Role.EMPLOYEE;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final AuthUtil authUtil;

    @Transactional
    public SignUpResponseDTO signUp(SignUpRequestDTO signUpRequestDTO) {

        // 1. 사용자 로그인 아이디 DB에 존재하는지 확인
        Optional<Member> findLoginId = memberRepository.findByLoginId(signUpRequestDTO.getLoginId());

        // 2. 로그인 아이디 중복 검사
        if (findLoginId.isPresent()) {
            throw new BusinessException(ErrorCode.MEMBER_PROFILE_DUPLICATION);
        }

        // 3. DB 저장 작업
        DeptType department = DeptType.formCode(signUpRequestDTO.getDeptCode());

        String hashedPassword = passwordEncoder.encode(signUpRequestDTO.getPassword()); // 보안적인 측면에서 사용자 비밀번호는 암호화해서 DB에 저장해야 함

        Member member = Member.builder()
                .userName(signUpRequestDTO.getUserName())
                .loginId(signUpRequestDTO.getLoginId())
                .password(hashedPassword)
                .role(EMPLOYEE)
                .department(new Department(department))
                .build();

        memberRepository.save(member);

        // 4. 클라이언트에 리턴
        return SignUpResponseDTO.builder()
                .userName(member.getUserName())
                .loginId(member.getLoginId())
                .department(member.getDepartment())
                .build();
    }

    @Transactional
    public VerifyResponseDTO verifyDeptCode(String deptCode) {

        // 1. 부서 검증
        DeptType department = DeptType.formCode(deptCode);


        // 2. 클라이언트에 리턴
        return VerifyResponseDTO.builder()
                .deptName(department.getDescription())
                .build();
    }

    @Transactional
    public GeneratedTokenDTO login(LoginRequestDTO loginRequestDTO) {

        // 1. 사용자가 입력한 아이디를 DB에 있는지 확인
        Optional<Member> findLoginId = memberRepository.findByLoginId(loginRequestDTO.getLoginId());

        // 2. Optional 객체가 비어있으면 존재하지 않는 아이디로 오류 반환
        if (findLoginId.isEmpty()) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 3. Optional 객체 꺼내서 Member 타입으로 변환
        Member member = findLoginId.get();

        // 4. 인코딩된 비밀번호, 사용자 입력 비밀번호 일치 확인
        if (!(passwordEncoder.matches(loginRequestDTO.getPassword(), member.getPassword()))) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 5. 토큰 생성시 필요한 DTO 생성
        SecurityMemberDTO securityMemberDTO = SecurityMemberDTO.builder()
                .id(member.getId())
                .loginId(member.getLoginId())
                .userName(member.getUserName())
                .role(member.getRole())
                .build();

        // 6. 토큰 생성 메서드로 리턴
        return jwtProvider.generateTokens(securityMemberDTO);
    }

    @Transactional
    public void logout() {

        // 1. 헤더에서 토큰 추출 및 유효성 검사
        String token = authUtil.extractTokenAfterTokenValidation();

        // 2. 토큰의 남은 시간 추출
        long expiration = jwtProvider.getRemainingExpiration(token);

        // 3. 키 : 액세스 토큰 _ 값 : "logout" _ 기간 : 남은 시간으로 레디스에 저장하여 로그아웃 토큰 관리
        redisUtil.setDataExpire(token, "logout", expiration);
    }
}
