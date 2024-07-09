package sunjin.DeptManagement_BackEnd.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.UpdateStatusOrderRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.DepartmentOrdersResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.GetAllOrderDTO;
import sunjin.DeptManagement_BackEnd.domain.order.service.AdminOrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {
    private final AdminOrderService adminOrderService;

    @GetMapping("/admin/orders")
    @Operation(summary = "[관리자] 신청된 모든 물품 조회", description = "신청된 모든 주문을 가져옵니다")
    public ResponseEntity<List<GetAllOrderDTO>> getAllOrder(){
        List<GetAllOrderDTO> response = adminOrderService.findAllOrder();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/orders/{departmentId}")
    @Operation(summary = "[관리자] 각 부서에서 신청된 모든 물품 조회", description = "URL의 departmentId를 통해 각 부서에서 신청된 주문을 가져옵니다.")
    public ResponseEntity<DepartmentOrdersResponseDTO> getAllOrder(@PathVariable("departmentId") Long departmentId){
        DepartmentOrdersResponseDTO response = adminOrderService.getAllOrdersByDepartment(departmentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/orders/{orderId}")
    @Operation(summary = "[관리자] 주문 상태 변경", description = "URL의 orderId를 통해 주문 상태를 변경합니다.")
    public ResponseEntity<String> updateOrderStatus(@RequestBody UpdateStatusOrderRequestDTO updateStatusOrderRequestDTO, @PathVariable("orderId") Long orderId) {
        adminOrderService.updateOrderStatus(updateStatusOrderRequestDTO, orderId);
        return ResponseEntity.ok("주문 상태가 변경되었습니다.");
    }
}
