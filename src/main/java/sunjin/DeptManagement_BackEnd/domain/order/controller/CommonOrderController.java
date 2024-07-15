package sunjin.DeptManagement_BackEnd.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.createOrderRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.service.CommonOrderService;
import sunjin.DeptManagement_BackEnd.domain.order.service.EmployeeOrderService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
public class CommonOrderController {
    private final CommonOrderService commonOrderService;
    private final EmployeeOrderService employeeOrderService;

    @PostMapping("/employee/orders")
    @Operation(summary = "[사원] 주문 신청", description = "물품 타입, 물품 이름, 개당 가격, 수량을 입력하면 주문 신청이 진행됩니다")
    public ResponseEntity<String> createOrder(@RequestPart(required = false, name = "image") MultipartFile image,
                                              @RequestPart(name = "request") @Valid createOrderRequestDTO createOrderRequestDTO){
        commonOrderService.createOrder(image, createOrderRequestDTO);
        return ResponseEntity.ok("주문에 성공했습니다.");
    }

    @GetMapping("/employee/submit")
    @Operation(summary = "[사원] 주문 상신", description = "주문을 선택하여 팀장에게 상신합니다.")
    public ResponseEntity<String> submitOrder(@RequestParam(value = "id", required = false) List<Long> id){
        employeeOrderService.submitOrder(id);
        return ResponseEntity.ok("상신에 성공했습니다.");
    }

    @GetMapping("/employee/orders")
    @Operation(summary = "[사원] 전체 및 상태별 조회", description = "전체 및 상태별 조회가 진행됩니다")
    public ResponseEntity<List<?>> getAllOrder(@RequestParam(value = "status", required = false) String status) {
        List<?> response = commonOrderService.getOrders(status);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/employee/{orderId}")
    @Operation(summary = "[사원] 주문 수정", description = "물품 타입, 물품 이름, 개당 가격, 수량를 입력하면 물품 수정이 진행됩니다")
    public ResponseEntity<String> updateOrder(@RequestBody @Valid createOrderRequestDTO createOrderRequestDTO,
                                              @PathVariable("orderId") Long orderId){
        commonOrderService.updateOrder(createOrderRequestDTO, orderId);
        return ResponseEntity.ok("주문 수정에 성공했습니다.");
    }

    @DeleteMapping("/employee/{orderId}")
    @Operation(summary = "[사원] 주문 삭제", description = "URL의 orderID를 통해 해당 물품 삭제가 진행됩니다.")
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId") Long orderId){
        commonOrderService.deleteOrder(orderId);
        return ResponseEntity.ok("주문 삭제에 성공했습니다.");
    }
}
