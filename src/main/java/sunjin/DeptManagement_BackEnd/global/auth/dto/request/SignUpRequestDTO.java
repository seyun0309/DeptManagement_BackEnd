package sunjin.DeptManagement_BackEnd.global.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpRequestDTO {

    @NotBlank
    private String deptCode;

    @NotBlank
    private String userName;

    @NotBlank
    private String loginId;

    @NotBlank
    private String password;
}
