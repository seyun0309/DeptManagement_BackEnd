package sunjin.DeptManagement_BackEnd.global.auth.dto;

import lombok.Builder;
import lombok.Getter;
import sunjin.DeptManagement_BackEnd.global.enums.Role;

@Getter
@Builder
public class GeneratedTokenDTO {
    private String accessToken;
    private String refreshToken;
    private String userName;
    private Role role;
}
