package sunjin.DeptManagement_BackEnd.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import sunjin.DeptManagement_BackEnd.global.enums.ProductStatusType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateStatusOrderRequestDTO {

    @NotBlank
    private String orderStatus;

    // 문자열을 ProductType enum으로 변환하는 메서드
    public ProductStatusType getProductTypeEnum() {
        return ProductStatusType.fromDescription(this.orderStatus);
    }
}
