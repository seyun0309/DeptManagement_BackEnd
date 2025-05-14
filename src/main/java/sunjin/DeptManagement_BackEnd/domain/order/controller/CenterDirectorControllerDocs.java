package sunjin.DeptManagement_BackEnd.domain.order.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.ApproveOrDeniedRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.DepartmentInfoResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.ProgressOrdersResponseDTO;

import java.util.List;

@Tag(name = "센터장 API", description = "(수정 모달)이미지 조회, (드롭 다운)부서/사원 정보 조회, 진행 중 주문 조회, 주문 승인/반려 처리")
public interface CenterDirectorControllerDocs {
    public ResponseEntity<String> getImg(@PathVariable("orderId") Long orderId);
    public ResponseEntity<List<DepartmentInfoResponseDTO>> getDepartment();
    public ResponseEntity<List<ProgressOrdersResponseDTO>> getFirstProgressOrders();
    public ResponseEntity<String> approveOrRejectOrderByTeamLeader(@PathVariable("orderId") Long orderId, @RequestBody ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO);
}
