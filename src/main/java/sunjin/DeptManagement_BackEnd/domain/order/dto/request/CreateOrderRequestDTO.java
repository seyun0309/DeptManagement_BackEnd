package sunjin.DeptManagement_BackEnd.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import sunjin.DeptManagement_BackEnd.global.enums.OrderType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderRequestDTO {

    @Schema(description = "비품, 간식, 일반 경비, 접대비, 교통비, 기타 중 선택")
    @NotBlank
    private String productType;

    @Schema(description = "상호명(예시. 쿠팡)")
    @NotBlank(message = "상호명을 입력해주세요")
    private String storeName;

    @Schema(description = "총 가격(숫자만)")
    @NotBlank(message = "총 가격을 입력해주세요")
    private int totalPrice;

    @Schema(description = "적요")
    private String description;

    // 문자열을 ProductType enum으로 변환하는 메서드
    public OrderType getProductTypeEnum() {
        return OrderType.fromDescription(this.productType);
    }
}
