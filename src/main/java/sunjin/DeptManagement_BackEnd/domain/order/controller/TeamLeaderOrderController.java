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

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TeamLeaderOrderController {
    private final CommonOrderService commonOrderService;
    private final TeamLeaderOrderService teamLeaderOrderService;

    @PostMapping("/teamleader/orders")
    @Operation(summary = "[팀장] 주문 신청", description = "물품 타입, 물품 이름, 개당 가격, 수량을 입력하면 주문 신청이 진행됩니다")
    public ResponseEntity<String> createOrder(@RequestPart(required = false, name = "image") MultipartFile image,
                                              @RequestPart(name = "request") @Valid createOrderRequestDTO createOrderRequestDTO){
        commonOrderService.createOrder(image, createOrderRequestDTO);
        return ResponseEntity.ok("주문에 성공했습니다.");
    }

    @GetMapping("/teamleader/{orderId}")
    @Operation(summary = "[팀장] 주문 상세 조회", description = "신청한 주문의 상세 내역을 조회합니다")
    public ResponseEntity<GetOrderDetailResponseDTO> getOrder(@PathVariable("orderId") Long orderId) {
        GetOrderDetailResponseDTO response = commonOrderService.getOrderDetails(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teamleader/submit")
    @Operation(summary = "[팀장] 주문 상신", description = "주문을 선택하여 센터장에게 상신합니다.")
    public ResponseEntity<String> submitOrder(@RequestParam(value = "id", required = false) List<Long> id){
        teamLeaderOrderService.submitOrder(id);
        return ResponseEntity.ok("상신에 성공했습니다.");
    }

    @GetMapping("/teamleader/orders")
    @Operation(summary = "[팀장] 전체 및 상태별 조회", description = "전체 및 상태별 조회가 진행됩니다")
    public ResponseEntity<List<?>> getAllOrder(@RequestParam(value = "status", required = false) String status) {
        List<?> response = commonOrderService.getOrders(status);
        return ResponseEntity.ok(response);
    }

    // 조회 버튼 클릭
    @GetMapping("/teamleader/department")
    @Operation(summary = "[팀장] 조회 버튼 클릭", description = "조회 버튼을 클릭하면 부서 이름, 사원 이름을 리턴합니다")
    public ResponseEntity<DepartmentInfoResponseDTO> getDepartment(){
        DepartmentInfoResponseDTO departmentResponseDTO = teamLeaderOrderService.getDepartmentInfo();
        return ResponseEntity.ok(departmentResponseDTO);
    }

    @GetMapping("/teamleader/department/details")
    @Operation(summary = "[팀장] 부서의 자세한 조회", description = "사원명, 주문 상태를 선택하면 그에 맞는 정보를 리턴합니다.")
    public ResponseEntity<List<?>> getDepartmentDetails(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "status", required = false) String status){
        List<?> response = teamLeaderOrderService.getDepartmentDetails(id, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teamleader/department/progress")
    @Operation(summary = "[팀장] 승인/반려 버튼 클릭", description = "승인/반려 버튼을 누르면 사원이 상신한 목록들을 가져옵니다")
    public ResponseEntity<List<ProgressOrdersResponseDTO>> getFirstProgressOrders() {
        List<ProgressOrdersResponseDTO> response = teamLeaderOrderService.getFirstProgressOrders();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/temleader/department/submit/{orderId}")
    @Operation(summary = "[팀장] 사원의 주문 승인/반려", description = "사원이 주문한 내역을 승인하여 센터장에게 올리거나 반려합니다")
    public ResponseEntity<String> approveOrRejectOrderByTeamLeader(@PathVariable("orderId") Long orderId, ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO) {
        teamLeaderOrderService.approveOrRejectOrderByTeamLeader(orderId, approveOrDeniedRequestDTO);
        return ResponseEntity.ok("처리가 완료되었습니다.");
    }

    @PatchMapping("/teamleader/{orderId}")
    @Operation(summary = "[팀장] 주문 수정", description = "물품 타입, 물품 이름, 개당 가격, 수량를 입력하면 물품 수정이 진행됩니다")
    public ResponseEntity<String> updateOrder(@RequestBody @Valid createOrderRequestDTO createOrderRequestDTO,
                                              @PathVariable("orderId") Long orderId){
        commonOrderService.updateOrder(createOrderRequestDTO, orderId);
        return ResponseEntity.ok("주문 수정에 성공했습니다.");
    }

    @DeleteMapping("/teamleader/{orderId}")
    @Operation(summary = "[팀장] 주문 삭제", description = "URL의 orderID를 통해 해당 물품 삭제가 진행됩니다.")
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId") Long orderId){
        commonOrderService.deleteOrder(orderId);
        return ResponseEntity.ok("주문 삭제에 성공했습니다.");
    }
}
