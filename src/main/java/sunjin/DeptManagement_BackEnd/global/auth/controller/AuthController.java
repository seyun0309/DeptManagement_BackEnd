package sunjin.DeptManagement_BackEnd.global.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sunjin.DeptManagement_BackEnd.global.auth.dto.GeneratedTokenDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.TokenModifyDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.request.LoginRequestDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.request.SignUpRequestDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.request.VerifyRequestDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.response.SignUpResponseDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.response.VerifyResponseDTO;
import sunjin.DeptManagement_BackEnd.global.auth.service.AuthService;
import sunjin.DeptManagement_BackEnd.global.auth.service.JwtProvider;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    @Operation(summary = "회원가입 로직", description = "부서코드, 사용자 이름, 아이디, 비밀번호를 입력하면 검증 후 회원가입을 진행합니다.")
    public ResponseEntity<SignUpResponseDTO> signUp(@RequestBody @Valid SignUpRequestDTO signUpRequestDTO) {
        SignUpResponseDTO responseDTO = authService.signUp(signUpRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/verify/department")
    @Operation(summary = "부서 검증 로직", description = "부서 코드를 검사합니다")
    public ResponseEntity<VerifyResponseDTO> verifyDept(@RequestBody VerifyRequestDTO verifyRequestDTO) {
        String deptCode = verifyRequestDTO.getDeptCode();
        VerifyResponseDTO verifyResponseDTO = authService.verifyDeptCode(deptCode);

        return ResponseEntity.ok(verifyResponseDTO);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 로직", description = "아이디, 비밀번호를 입력하면 검증 후 로그인을 진행하고 성공하면 access Token, Refresh Token을 발급합니다.")
    public ResponseEntity<GeneratedTokenDTO> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        GeneratedTokenDTO generatedTokenDTO = authService.login(loginRequestDTO);
        return ResponseEntity.ok(generatedTokenDTO);

    }

    @PatchMapping("/logout")
    @Operation(summary = "로그아웃 로직", description = "사용자의 Refresh Token을 무효화합니다.")
    public ResponseEntity<String> logout() {
        authService.logout();
        return ResponseEntity.ok("로그아웃이 정상적으로 되었습니다");
    }

    @PatchMapping("/tokens")
    @Operation(summary = "토큰 재발급", description = "Access Token과 남은 기간에 따라 Refresh Token을 재발급 합니다.")
    public GeneratedTokenDTO tokenModify(@Valid @RequestBody TokenModifyDTO tokenModifyRequest) {
        return jwtProvider.reissueToken(tokenModifyRequest.getRefreshToken());
    }
}
