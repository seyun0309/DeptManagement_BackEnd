package sunjin.DeptManagement_BackEnd.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.CreateOrderRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.GetOrderDetailResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.service.CommonOrderService;
import sunjin.DeptManagement_BackEnd.domain.order.service.EmployeeOrderService;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.io.IOException;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
public class EmployeeOrderController {
    private final CommonOrderService commonOrderService;
    private final EmployeeOrderService employeeOrderService;

    @PostMapping("/employee/orders")
    @Operation(summary = "[사원] 주문 신청", description = "물품 타입, 물품 이름, 개당 가격, 수량을 입력하면 주문 신청이 진행됩니다")
    public ResponseEntity<String> createOrder(@RequestPart(name = "image") MultipartFile image,
                                              @RequestPart(name = "request") @Valid CreateOrderRequestDTO createOrderRequestDTO){
        commonOrderService.createOrder(image, createOrderRequestDTO);
        return ResponseEntity.ok("주문에 성공했습니다.");
    }

    @GetMapping("/employee/{orderId}")
    @Operation(summary = "[사원] 본인의 주문 상세 조회", description = "신청한 주문의 상세 내역을 조회합니다")
    public ResponseEntity<GetOrderDetailResponseDTO> getOrder(@PathVariable("orderId") Long orderId) throws IOException {
        GetOrderDetailResponseDTO response = commonOrderService.getOrderDetails(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/submit")
    @Operation(summary = "[사원] 본인의 주문을 팀장에게 상신", description = "주문을 선택하여 팀장에게 상신합니다.")
    public ResponseEntity<String> submitOrder(@RequestParam(value = "order", required = false) List<Long> ids){
        employeeOrderService.submitOrder(ids);
        return ResponseEntity.ok("상신에 성공했습니다.");
    }

    @GetMapping("/employee/orders")
    @Operation(summary = "[사원] 본인의 주문을 전체 및 상태별 조회", description = "전체 및 상태별 조회가 진행됩니다")
    public ResponseEntity<List<?>> getAllOrder(@RequestParam(value = "status", required = false) List<String> statuses) {
        List<?> response = commonOrderService.getOrders(statuses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/img/{orderId}")
    @Operation(summary = "[사원] 수정 모달에 띄울 이미지 리턴", description = "수정 버튼을 클릭하면 해당 주문의 사진을 리턴합니다")
    public ResponseEntity<String> getImg(@PathVariable("orderId") Long orderId) {
        String response = commonOrderService.getImg(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/employee/{orderId}")
    @Operation(summary = "[사원] 본인의 주문 수정", description = "물품 타입, 물품 이름, 개당 가격, 수량를 입력하면 물품 수정이 진행됩니다")
    public ResponseEntity<String> updateOrder(@RequestPart(name = "image") MultipartFile image,
                                              @RequestPart(name = "request") @Valid CreateOrderRequestDTO createOrderRequestDTO,
                                              @PathVariable("orderId") Long orderId){
        commonOrderService.updateOrder(image, createOrderRequestDTO, orderId);
        return ResponseEntity.ok("주문 수정에 성공했습니다.");
    }

    @DeleteMapping("/employee/{orderId}")
    @Operation(summary = "[사원] 본인의 주문 삭제", description = "URL의 orderID를 통해 해당 물품 삭제가 진행됩니다.")
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId") Long orderId){
        commonOrderService.deleteOrder(orderId);
        return ResponseEntity.ok("주문 삭제에 성공했습니다.");
    }
}
