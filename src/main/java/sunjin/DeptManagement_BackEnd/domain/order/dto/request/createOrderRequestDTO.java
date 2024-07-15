package sunjin.DeptManagement_BackEnd.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import sunjin.DeptManagement_BackEnd.global.enums.OrderType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class createOrderRequestDTO {

    @NotBlank
    private String productType;

    @NotBlank(message = "상호명을 입력해주세요")
    private String storeName;

    @NotBlank(message = "총 가격을 입력해주세요")
    private int totalPrice;

    // 적요
    private  String description;

    // 문자열을 ProductType enum으로 변환하는 메서드
    public OrderType getProductTypeEnum() {
        return OrderType.fromDescription(this.productType);
    }
}
