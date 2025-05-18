package sunjin.DeptManagement_BackEnd.global.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReissuedTokenResponseDTO {
    private String accessToken;
    private String refreshToken;
}
