package sunjin.DeptManagement_BackEnd.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
@Builder
public class GetOrderDetailResponseDTO {
    private String DeptName;
    private String applicantName;
    private String orderType;
    private String storeName;
    private int totalPrice;
    private String description;
    private String createdAt;
    private String firstProcDate;
    private String secondProcDate;
    private String rejectionDescription;
    private String receiptImgUrl;
}
