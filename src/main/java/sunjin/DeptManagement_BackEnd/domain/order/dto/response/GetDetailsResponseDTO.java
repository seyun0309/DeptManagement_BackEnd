package sunjin.DeptManagement_BackEnd.domain.order.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class GetDetailsResponseDTO {
    DepartmentResponseDTO departmentResponseDTO;
    List<MemberResponseDTO> memberResponseDTOList;
}
