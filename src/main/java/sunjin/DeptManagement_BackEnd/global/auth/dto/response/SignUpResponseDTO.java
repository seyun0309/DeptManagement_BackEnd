package sunjin.DeptManagement_BackEnd.global.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;

@Getter
@AllArgsConstructor
@Builder
public class SignUpResponseDTO {
    private String userName;
    private String loginId;
    private Department department;
}
