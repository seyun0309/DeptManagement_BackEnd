package sunjin.DeptManagement_BackEnd.global.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import sunjin.DeptManagement_BackEnd.global.auth.dto.GeneratedTokenDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.request.LoginRequestDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.request.SignUpRequestDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.request.VerifyRequestDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.response.ReissuedTokenResponseDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.response.SignUpResponseDTO;
import sunjin.DeptManagement_BackEnd.global.auth.dto.response.VerifyResponseDTO;

@Tag(name = "인증 인가 API", description = "회원가입, 부서 코드 인증, 로그인, 로그아웃, 토큰 재발급")
public interface AuthControllerDocs {
    @Operation(summary = "회원가입", description = "필요 파라미터 : 부서코드, 사용자 이름, 아이디, 비밀번호")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = SignUpResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "아이디 중복", content = @Content)
    })
    public ResponseEntity<SignUpResponseDTO> signUp(@RequestBody @Valid SignUpRequestDTO signUpRequestDTO);

    @Operation(summary = "부서 인증", description = "필요 파라미터 : 부서코드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "부서 인증 성공", content = @Content(schema = @Schema(implementation = VerifyResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 부서", content = @Content)
    })
    public ResponseEntity<VerifyResponseDTO> verifyDept(@RequestBody VerifyRequestDTO verifyRequestDTO);


    @Operation(summary = "로그인", description = "필요 파라미터 : 아이디, 비밀번호")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "로그인 성공", content = @Content(schema = @Schema(implementation = LoginRequestDTO.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 아이디", content = @Content),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치", content = @Content)
    })
    public ResponseEntity<GeneratedTokenDTO> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO);


    @Operation(summary = "로그아웃", description = "필요 파라미터 : X")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "401", description = "이미 로그아웃된 토큰", content = @Content)
    })
    public ResponseEntity<String> logout();


    @Operation(summary = "토큰 재발급", description = "필요 파라미터 : 리프래시 토큰")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "만료된 리프레시 토큰 또는 잘못된 JWT 서명"),
            @ApiResponse(responseCode = "403", description = "지원되지 않는 JWT 토큰"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (비어있거나 잘못된 토큰 형식)"),
            @ApiResponse(responseCode = "404", description = "사용자 정보 없음 또는 리프레시 토큰 불일치")
    })
    @Parameters(value = {
            @Parameter(
                    name = "Refresh-Token",
                    description = "헤더에 포함된 리프레시 토큰",
                    required = true,
                    in = ParameterIn.HEADER
            )
    })
    public ReissuedTokenResponseDTO tokenModify(@RequestHeader("Refresh-Token") String refreshToken);
}
