package sunjin.DeptManagement_BackEnd.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import sunjin.DeptManagement_BackEnd.global.enums.ProductType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class createOrderRequestDTO {

    @NotBlank
    private String productType;

    @NotBlank
    private String productName;

    @NotBlank
    private int price;

    @NotBlank
    private int quantity;

    // 문자열을 ProductType enum으로 변환하는 메서드
    public ProductType getProductTypeEnum() {
        return ProductType.fromDescription(this.productType);
    }
}
