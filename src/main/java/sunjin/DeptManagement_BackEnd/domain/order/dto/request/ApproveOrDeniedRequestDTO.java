package sunjin.DeptManagement_BackEnd.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApproveOrDeniedRequestDTO {
    private String isApproved;

    private String deniedDescription;
}
