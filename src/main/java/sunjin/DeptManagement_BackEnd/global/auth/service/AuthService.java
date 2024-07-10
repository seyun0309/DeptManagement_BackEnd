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
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.enums.Role;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.util.Objects;
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
        Department department = departmentRepository.findByDeptCode(signUpRequestDTO.getDeptCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        String hashedPassword = passwordEncoder.encode(signUpRequestDTO.getPassword());
        Member member = Member.builder()
                .userName(signUpRequestDTO.getUserName())
                .loginId(signUpRequestDTO.getLoginId())
                .password(hashedPassword)
                .role(EMPLOYEE)
                .department(department)
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
        Department department = departmentRepository.findByDeptCode(deptCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

        return VerifyResponseDTO.builder()
                .deptName(department.getDeptName())
                .build();
    }

    public GeneratedTokenDTO login(LoginRequestDTO loginRequestDTO) {
        Optional<Member> findLoginId = memberRepository.findByLoginId(loginRequestDTO.getLoginId());

        if (findLoginId.isPresent()) {
            Member member = findLoginId.get();

            // 관리자 아이디로 로그인한 경우
            if (loginRequestDTO.getLoginId().equals("admin") && loginRequestDTO.getPassword().equals("1234")) {
                if (member.getRole() != Role.ADMIN) { // 현재 Role이 ADMIN이 아니면 변경
                    member.setRole(Role.ADMIN);
                    memberRepository.save(member); // 변경된 Role 저장
                }
            }

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
