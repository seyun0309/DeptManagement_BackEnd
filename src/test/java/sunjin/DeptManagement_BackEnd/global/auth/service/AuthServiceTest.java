package sunjin.DeptManagement_BackEnd.global.auth.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import sunjin.DeptManagement_BackEnd.domain.department.repository.DepartmentRepository;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.global.auth.dto.response.VerifyResponseDTO;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@Transactional
class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private DepartmentRepository departmentRepository;

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
    public void 부서인증_성공() {
        //given
        String deptCode = "0123";

        //when
        VerifyResponseDTO result = authService.verifyDeptCode(deptCode);

        //then
        assertEquals("디지털 SCM", result.getDeptName());
    }

    @Test
    public void 부서인증_실패() {
        //given
        String deptCode = "9999";

        //when && then
        assertThrows(BusinessException.class, () -> {
            authService.verifyDeptCode(deptCode);
        });
    }
}