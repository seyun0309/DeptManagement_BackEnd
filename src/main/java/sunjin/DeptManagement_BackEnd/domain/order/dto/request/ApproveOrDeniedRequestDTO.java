package sunjin.DeptManagement_BackEnd.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApproveOrDeniedRequestDTO {

    @Schema(description = "승인(true) or 반려")
    private String isApproved;

    @Schema(description = "반려 사유")
    private String deniedDescription;
}
