package sunjin.DeptManagement_BackEnd.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class DepartmentOrdersResponseDTO {
    private List<GetAllOrderDTO> orders;
    private int totalAmount;
}
