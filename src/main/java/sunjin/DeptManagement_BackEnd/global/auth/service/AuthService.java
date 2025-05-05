package sunjin.DeptManagement_BackEnd.global.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.department.repository.DepartmentRepository;
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
import sunjin.DeptManagement_BackEnd.global.enums.Role;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.util.Optional;

import static sunjin.DeptManagement_BackEnd.global.enums.Role.EMPLOYEE;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponseDTO signUp(SignUpRequestDTO signUpRequestDTO) {
        Optional<Member> findLoginId = memberRepository.findByLoginId(signUpRequestDTO.getLoginId());

        //아이디 중복 검사
        if (findLoginId.isPresent()) {
            throw new BusinessException(ErrorCode.MEMBER_PROFILE_DUPLICATION);
        }

        //deptCode 통해서 부서 내용 가져오기
        DeptType department = DeptType.formCode(signUpRequestDTO.getDeptCode());

        String hashedPassword = passwordEncoder.encode(signUpRequestDTO.getPassword());
        Member member = Member.builder()
                .userName(signUpRequestDTO.getUserName())
                .loginId(signUpRequestDTO.getLoginId())
                .password(hashedPassword)
                .role(EMPLOYEE)
                .department(new Department(department))
                .build();

        memberRepository.save(member);

        return SignUpResponseDTO.builder()
                .userName(member.getUserName())
                .loginId(member.getLoginId())
                .department(member.getDepartment())
                .build();
    }

    @Transactional
    public VerifyResponseDTO verifyDeptCode(String deptCode) {
        DeptType department = DeptType.formCode(deptCode);

        return VerifyResponseDTO.builder()
                .deptName(department.getDescription())
                .build();
    }

    public GeneratedTokenDTO login(LoginRequestDTO loginRequestDTO) {
        Optional<Member> findLoginId = memberRepository.findByLoginId(loginRequestDTO.getLoginId());

        if (findLoginId.isPresent()) {
            Member member = findLoginId.get();

            if (passwordEncoder.matches(loginRequestDTO.getPassword(), member.getPassword())) {
                SecurityMemberDTO securityMemberDTO = SecurityMemberDTO.builder()
                        .id(member.getId())
                        .loginId(member.getLoginId())
                        .userName(member.getUserName())
                        .role(member.getRole())
                        .build();

                return jwtProvider.generateTokens(securityMemberDTO);
            } else {
                throw new BusinessException(ErrorCode.INVALID_PASSWORD);
            }
        } else {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Transactional
    public void logout() {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.setRefreshToken(null);
        memberRepository.save(member);
    }
}
