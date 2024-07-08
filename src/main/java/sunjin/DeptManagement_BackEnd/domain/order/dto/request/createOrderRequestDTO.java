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
    private ProductType productType;

    @NotBlank
    private String productName;

    @NotBlank
    private int price;

    @NotBlank
    private int quantity;
}
