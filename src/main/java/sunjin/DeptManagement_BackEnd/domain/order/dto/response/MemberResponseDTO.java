package sunjin.DeptManagement_BackEnd.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MemberResponseDTO {
    private Long memberId;
    private String memberName;
}
