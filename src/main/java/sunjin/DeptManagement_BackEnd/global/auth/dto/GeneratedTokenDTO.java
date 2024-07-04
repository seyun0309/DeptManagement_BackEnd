package sunjin.DeptManagement_BackEnd.global.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeneratedTokenDTO {
    private String accessToken;
    private String refreshToken;
}
