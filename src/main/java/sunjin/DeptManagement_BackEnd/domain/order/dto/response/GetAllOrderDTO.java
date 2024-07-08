package sunjin.DeptManagement_BackEnd.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sunjin.DeptManagement_BackEnd.global.enums.ProductStatusType;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class GetAllOrderDTO {
    private Long orderId;
    private String createTime;
    private String productType;
    private String productName;
    private int price;
    private int quantity;
    private int totalPrice;
    private ProductStatusType status;
    private String processDate;
    private String applicant;
    private String applicantDeptName;
}
