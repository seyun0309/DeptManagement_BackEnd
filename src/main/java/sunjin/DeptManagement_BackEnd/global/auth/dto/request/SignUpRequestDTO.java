package sunjin.DeptManagement_BackEnd.global.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpRequestDTO {

    @NotBlank(message = "부서 코드를 입력해주세요")
    private String deptCode;

    @NotBlank(message = "이름을 입력해주세요")
    private String userName;

    @NotBlank(message = "아이디를 입력해주세요")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}
