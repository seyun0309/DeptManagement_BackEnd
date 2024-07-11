package sunjin.DeptManagement_BackEnd.domain.receipt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DepartmentReceiptResponseDTO {
    private Long receiptId;
    private String receiptDate;
    private String receiptName;
    private int receiptPrice;
}
