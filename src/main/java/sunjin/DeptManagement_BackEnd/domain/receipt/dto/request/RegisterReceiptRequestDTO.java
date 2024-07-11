package sunjin.DeptManagement_BackEnd.domain.receipt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterReceiptRequestDTO {
    @NotBlank(message = "영수증에 있는 가게명을 입력해주세요")
    private String storeName;

    @NotBlank(message = "영수증에 있는 총 금액을 입력해주세요")
    private String receiptPrice;
}
