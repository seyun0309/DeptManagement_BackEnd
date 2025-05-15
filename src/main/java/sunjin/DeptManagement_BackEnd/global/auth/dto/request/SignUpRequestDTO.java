package sunjin.DeptManagement_BackEnd.global.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpRequestDTO {

    @Schema(description = "부서 코드")
    @NotBlank(message = "부서 코드를 입력해주세요")
    private String deptCode;

    @Schema(description = "사용자명")
    @NotBlank(message = "이름을 입력해주세요")
    private String userName;

    @Schema(description = "아이디")
    @NotBlank(message = "아이디를 입력해주세요")
    private String loginId;

    @Schema(description = "비밀번호")
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}
