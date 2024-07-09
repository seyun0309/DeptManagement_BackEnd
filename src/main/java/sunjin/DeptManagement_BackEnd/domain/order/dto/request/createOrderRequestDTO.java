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

    @NotBlank(message = "주문 하시는 상품의 이름을 입력해주세요")
    private String productName;

    @NotBlank(message = "해당 상품의 개당 가격을 입력해주세요")
    private int price;

    @NotBlank(message = "총 몇 개의 상품을 구매하는지 적어주세요")
    private int quantity;

    // 문자열을 ProductType enum으로 변환하는 메서드
    public ProductType getProductTypeEnum() {
        return ProductType.fromDescription(this.productType);
    }
}
