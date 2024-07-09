package sunjin.DeptManagement_BackEnd.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GetAllOrderDTO {
    private Long orderId;
    private String latestTime;
    private String productType;
    private String productName;
    private int price;
    private int quantity;
    private int totalPrice;
    private String status;
    private String processDate;
    private String applicant;
    private String applicantDeptName;
}
