package sunjin.DeptManagement_BackEnd.global.auth.dto;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sunjin.DeptManagement_BackEnd.global.enums.Role;

@Getter
@Builder
@AllArgsConstructor
public class SecurityMemberDTO {
    private final Long id;
    private final String loginId;
    private final String userName;
    @Setter
    private Role role;

    public static SecurityMemberDTO fromClaims(Claims claims) {
        return SecurityMemberDTO.builder()
                .id(Long.valueOf(claims.getId()))
                .loginId(claims.get("loginId", String.class))
                .userName(claims.get("userName", String.class))
                .role(Role.valueOf(claims.get("role", String.class))).build();
    }
}
