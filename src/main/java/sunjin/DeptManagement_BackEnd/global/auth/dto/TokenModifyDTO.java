package sunjin.DeptManagement_BackEnd.global.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TokenModifyDTO {
    @NotBlank
    @Schema(description = "Refresh Token")
    private String refreshToken;
}
