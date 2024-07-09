package sunjin.DeptManagement_BackEnd.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.createOrderRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.DepartmentOrdersResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.service.CommonOrderService;


@RestController
@RequiredArgsConstructor
@Slf4j
public class CommonOrderController {
    private final CommonOrderService commonOrderService;

    @PostMapping("/api/orders")
    @Operation(summary = "[회원] 주문 신청 로직", description = "물품 타입, 물품 이름, 개당 가격, 수량을 입력하면 주문 신청이 진행됩니다")
    public ResponseEntity<String> createOrder(@RequestBody @Valid createOrderRequestDTO createOrderRequestDTO){
        commonOrderService.createOrder(createOrderRequestDTO);
        return ResponseEntity.ok("주문에 성공했습니다.");
    }

    @GetMapping("/api/orders")
    @Operation(summary = "[회원] 본인 부서에서 신청된 모든 주문 조회",
            description = "URL의 departmentID를 통해 해당 부서의 이름으로 신청된 모든 주문 조회가 진행됩니다")
    public ResponseEntity<DepartmentOrdersResponseDTO> getAllOrder(){
        DepartmentOrdersResponseDTO response = commonOrderService.getAllOrdersByDepartment();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/orders/{orderId}")
    @Operation(summary = "신청한 주문 수정 로직", description = "물품 타입, 물품 이름, 개당 가격, 수량를 입력하면 물품 수정이 진행됩니다")
    public ResponseEntity<String> updateOrder(@RequestBody @Valid createOrderRequestDTO createOrderRequestDTO,
                                              @PathVariable("orderId") Long orderId){
        commonOrderService.updateOrder(createOrderRequestDTO, orderId);
        return ResponseEntity.ok("주문 수정에 성공했습니다.");
    }

    @PostMapping("/api/orders/{orderId}")
    @Operation(summary = "주문 삭제 로직", description = "URL의 orderID를 통해 해당 물품 삭제가 진행됩니다.")
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId") Long orderId){
        commonOrderService.deleteOrder(orderId);
        return ResponseEntity.ok("주문 삭제에 성공했습니다.");
    }
}
