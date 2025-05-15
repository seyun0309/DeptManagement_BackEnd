package sunjin.DeptManagement_BackEnd.global.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequestDTO {

    @Schema(description = "아이디")
    @NotBlank(message = "아이디를 입력해주세요")
    private String loginId;

    @Schema(description = "비밀번호")
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}
