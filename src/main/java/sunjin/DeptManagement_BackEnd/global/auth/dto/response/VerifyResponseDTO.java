package sunjin.DeptManagement_BackEnd.global.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class VerifyResponseDTO {
    private String deptName;
}
