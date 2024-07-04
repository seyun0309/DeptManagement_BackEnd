package sunjin.DeptManagement_BackEnd.global.auth.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyRequestDTO {
    private String deptCode;
}
