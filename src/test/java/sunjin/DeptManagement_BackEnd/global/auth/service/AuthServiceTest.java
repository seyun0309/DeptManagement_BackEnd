package sunjin.DeptManagement_BackEnd.global.auth.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.global.auth.dto.GeneratedTokenDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.SecurityMemberDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.request.LoginRequestDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.request.SignUpRequestDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.response.SignUpResponseDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.response.VerifyResponseDTO;
import sunjin.DeptManagement_BackEnd.global.enums.Role;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


@SpringBootTest
@Transactional
class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {

    }

    @AfterEach
    public void tearDown() {

    }

    @Test
    @DisplayName("부서 코드 인증 성공")
    public void 부서인증_성공() {
        //given
        String deptCode = "0123";

        //when
        VerifyResponseDTO result = authService.verifyDeptCode(deptCode);

        //then
        assertEquals("디지털 SCM", result.getDeptName());
    }

    @Test
    @DisplayName("부서 코드 인증 실패")
    public void 부서인증_실패() {
        //given
        String deptCode = "9999";

        //when && then
        assertThrows(BusinessException.class, () -> {
            authService.verifyDeptCode(deptCode);
        });
    }

    @Test
    @DisplayName("회원가입 성공")
    public void 회원가입_성공() {
        //given
        SignUpRequestDTO response = SignUpRequestDTO.builder()
                .deptCode("0123")
                .userName("김세윤")
                .loginId("asdf1234")
                .password("fjdk1oi23j")
                .build();
        //when
        SignUpResponseDTO result = authService.signUp(response);

        //then
        assertEquals("김세윤", result.getUserName());
        assertEquals("asdf1234", result.getLoginId());
        assertEquals("디지털 SCM", result.getDepartment().getDepartment().getDescription());
    }

    @Test
    @DisplayName("회원가입 실패 - 아이디 중복")
    public void 회원가입_실패_아이디_중복() {
        //given
        SignUpRequestDTO request = SignUpRequestDTO.builder()
                .deptCode("0123")
                .userName("김세윤")
                .loginId("asdf1234")
                .password("fjdk1oi23j")
                .build();

        given(memberRepository.findByLoginId("asdf1234"))
                .willReturn(Optional.of(mock(Member.class)));

        // when & then
        assertThrows(BusinessException.class, () -> authService.signUp(request));
    }

    @Test
    @DisplayName("로그인 성공")
    public void 로그인_성공(){
        // given
        LoginRequestDTO request = LoginRequestDTO.builder()
                .loginId("asdf1234")
                .password("password123")
                .build();

        Member mockMember = Member.builder()
                .loginId("asdf1234")
                .userName("김세윤")
                .password("encodedPassword")
                .role(Role.EMPLOYEE)
                .build();

        // DB에서 아이디 찾으면 이 멤버 리턴
        given(memberRepository.findByLoginId("asdf1234"))
                .willReturn(Optional.of(mockMember));

        // 비번 비교가 성공하는 것으로 설정
        given(passwordEncoder.matches("password123", "encodedPassword"))
                .willReturn(true);

        // 토큰 생성도 성공하는 걸로 설정
        GeneratedTokenDTO tokenDTO = GeneratedTokenDTO.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .userName("김세윤")
                .role(Role.EMPLOYEE)
                .build();

        given(jwtProvider.generateTokens(any(SecurityMemberDTO.class)))
                .willReturn(tokenDTO);

        // when
        GeneratedTokenDTO result = authService.login(request);

        // then
        assertEquals("access", result.getAccessToken());
        assertEquals("refresh", result.getRefreshToken());
    }


    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 아이디")
    public void 로그인_실패_아이디없음() {
        // given
        LoginRequestDTO request = LoginRequestDTO.builder()
                .loginId("asdf1234")
                .password("adkjfk123")
                .build();

        given(memberRepository.findByLoginId("asdf1234"))
                .willReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 오류")
    public void 로그인_실패_비밀번호오류() {
        // given
        LoginRequestDTO request = LoginRequestDTO.builder()
                .loginId("asdf1234")
                .password("wrongPassword")
                .build();

        Member mockMember = Member.builder()
                .loginId("asdf1234")
                .userName("김세윤")
                .password("encodedPassword")
                .role(Role.EMPLOYEE)
                .build();

        given(memberRepository.findByLoginId("asdf1234"))
                .willReturn(Optional.of(mockMember));

        // 비밀번호 매칭 실패하도록 설정
        given(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .willReturn(false);

        // when & then
        assertThrows(BusinessException.class, () -> authService.login(request));
    }
}