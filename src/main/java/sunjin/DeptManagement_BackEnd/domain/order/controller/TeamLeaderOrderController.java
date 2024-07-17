package sunjin.DeptManagement_BackEnd.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.ApproveOrDeniedRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.createOrderRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.DepartmentInfoResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.GetOrderDetailResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.ProgressOrdersResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.service.CommonOrderService;
import sunjin.DeptManagement_BackEnd.domain.order.service.TeamLeaderOrderService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TeamLeaderOrderController {
    private final CommonOrderService commonOrderService;
    private final TeamLeaderOrderService teamLeaderOrderService;

    @PostMapping("/teamleader/orders")
    @Operation(summary = "[팀장] 주문 신청")
    public ResponseEntity<String> createOrder(@RequestPart(required = false, name = "image") MultipartFile image,
                                              @RequestPart(name = "request") @Valid createOrderRequestDTO createOrderRequestDTO){
        commonOrderService.createOrder(image, createOrderRequestDTO);
        return ResponseEntity.ok("주문에 성공했습니다.");
    }

    @GetMapping("/teamleader/{orderId}")
    @Operation(summary = "[팀장] 본인의 주문 상세 조회")
    public ResponseEntity<GetOrderDetailResponseDTO> getOrder(@PathVariable("orderId") Long orderId) throws IOException {
        GetOrderDetailResponseDTO response = commonOrderService.getOrderDetails(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teamleader/submit")
    @Operation(summary = "[팀장] 본인의 주문을 센터장에게 상신")
    public ResponseEntity<String> submitOrder(@RequestParam(value = "order", required = false) List<Long> ids){
        teamLeaderOrderService.submitOrder(ids);
        return ResponseEntity.ok("상신에 성공했습니다.");
    }

    @GetMapping("/teamleader/orders")
    @Operation(summary = "[팀장] 본인의 주문을 전체 및 상태별 조회")
    public ResponseEntity<List<?>> getAllOrder(@RequestParam(value = "status", required = false) String status) {
        List<?> response = commonOrderService.getOrders(status);
        return ResponseEntity.ok(response);
    }

    // 조회 버튼 클릭
    @GetMapping("/teamleader/department")
    @Operation(summary = "[팀장] 부서 이름, 사원 이름 리턴 -> 드롭다운에 적용", description = "조회 버튼을 클릭하면 부서 이름, 사원 이름을 리턴합니다")
    public ResponseEntity<DepartmentInfoResponseDTO> getDepartment(){
        DepartmentInfoResponseDTO departmentResponseDTO = teamLeaderOrderService.getDepartmentInfo();
        return ResponseEntity.ok(departmentResponseDTO);
    }

    @GetMapping("/teamleader/department/details")
    @Operation(summary = "[팀장] 사원명, 상태를 적절히 골라 조회 진행 후 DTO 리턴")
    public ResponseEntity<List<?>> getDepartmentDetails(
            @RequestParam(value = "member", required = false) String memberId,
            @RequestParam(value = "status", required = false) String status){
        List<?> response = teamLeaderOrderService.getDepartmentDetails(Long.parseLong(memberId), status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teamleader/department/progress")
    @Operation(summary = "[팀장] 사원이 팀장에게 상신한 목록들 가져옴")
    public ResponseEntity<List<ProgressOrdersResponseDTO>> getFirstProgressOrders() {
        List<ProgressOrdersResponseDTO> response = teamLeaderOrderService.getFirstProgressOrders();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/temleader/department/submit/{orderId}")
    @Operation(summary = "[팀장] 사원의 주문을 승인/반려 처리함")
    public ResponseEntity<String> approveOrRejectOrderByTeamLeader(@PathVariable("orderId") Long orderId, ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO) {
        teamLeaderOrderService.approveOrRejectOrderByTeamLeader(orderId, approveOrDeniedRequestDTO);
        return ResponseEntity.ok("처리가 완료되었습니다.");
    }

    @PatchMapping("/teamleader/{orderId}")
    @Operation(summary = "[팀장] 본인의 주문 수정")
    public ResponseEntity<String> updateOrder(@RequestPart(name = "image") MultipartFile image,
                                              @RequestPart(name = "request") @Valid createOrderRequestDTO createOrderRequestDTO,
                                              @PathVariable("orderId") Long orderId){
        commonOrderService.updateOrder(image, createOrderRequestDTO, orderId);
        return ResponseEntity.ok("주문 수정에 성공했습니다.");
    }

    @DeleteMapping("/teamleader/{orderId}")
    @Operation(summary = "[팀장] 본인의 주문 삭제")
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId") Long orderId){
        commonOrderService.deleteOrder(orderId);
        return ResponseEntity.ok("주문 삭제에 성공했습니다.");
    }
}
