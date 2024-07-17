package sunjin.DeptManagement_BackEnd.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DeniedOrdersResponseDTO {
    private Long orderId;
    private String applicantDeptName;
    private String applicant;
    private String productType;
    private String storeName;
    private int totalPrice;
    private String description;
    private String orderStatus;
    private String deniedDescription;
    private String createdAt;
    private String procDate;
}
