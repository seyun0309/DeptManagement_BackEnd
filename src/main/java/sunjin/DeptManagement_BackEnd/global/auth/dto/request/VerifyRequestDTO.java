package sunjin.DeptManagement_BackEnd.global.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyRequestDTO {

    @Schema(description = "부서코드")
    @NotBlank
    private String deptCode;
}
