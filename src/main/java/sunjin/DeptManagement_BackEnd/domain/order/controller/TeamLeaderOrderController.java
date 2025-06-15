package sunjin.DeptManagement_BackEnd.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.ApproveOrDeniedRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.CreateOrderRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.DepartmentInfoResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.GetOrderDetailResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.FirstProgressOrdersResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.service.CommonOrderService;
import sunjin.DeptManagement_BackEnd.domain.order.service.TeamLeaderOrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TeamLeaderOrderController implements TeamLeaderOrderControllerDocs{
    private final CommonOrderService commonOrderService;
    private final TeamLeaderOrderService teamLeaderOrderService;

    @PostMapping("/teamleader/orders")
    @Operation(summary = "[팀장] 주문 신청")
    public ResponseEntity<String> createOrder(@RequestPart(required = false, name = "image") MultipartFile image,
                                              @RequestPart(name = "request") @Valid CreateOrderRequestDTO createOrderRequestDTO){
        commonOrderService.createOrder(image, createOrderRequestDTO);
        return ResponseEntity.ok("주문에 성공했습니다.");
    }

    @GetMapping("/teamleader/{orderId}")
    @Operation(summary = "[팀장] 본인의 주문 상세 조회")
    public ResponseEntity<GetOrderDetailResponseDTO> getOrder(@PathVariable("orderId") Long orderId) {
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
    public ResponseEntity<List<?>> getAllOrder(@RequestParam(value = "status", required = false) List<String> statuses) {
        List<?> response = commonOrderService.getOrders(statuses);
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
            @RequestParam(value = "member", required = false) Long memberId,
            @RequestParam(value = "status", required = false) List<String> statuses){
        List<?> response = teamLeaderOrderService.getDepartmentDetails(memberId, statuses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teamleader/department/progress")
    @Operation(summary = "[팀장] 사원이 팀장에게 상신한 목록들 가져옴")
    public ResponseEntity<List<FirstProgressOrdersResponseDTO>> getFirstProgressOrders() {
        List<FirstProgressOrdersResponseDTO> response = teamLeaderOrderService.getFirstProgressOrders();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/temleader/department/submit/{orderId}")
    @Operation(summary = "[팀장] 사원의 주문을 승인/반려 처리함")
    public ResponseEntity<String> approveOrRejectOrderByTeamLeader(@PathVariable("orderId") Long orderId, @RequestBody ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO) {
        teamLeaderOrderService.approveOrRejectOrderByTeamLeader(orderId, approveOrDeniedRequestDTO);
        return ResponseEntity.ok("처리가 완료되었습니다.");
    }

    @GetMapping("/teamleader/img/{orderId}")
    @Operation(summary = "[팀장] 수정 모달에 띄울 이미지 리턴", description = "수정 버튼을 클릭하면 해당 주문의 사진을 리턴합니다")
    public ResponseEntity<String> getImg(@PathVariable("orderId") Long orderId) {
        String response = commonOrderService.getImg(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/teamleader/{orderId}")
    @Operation(summary = "[팀장] 본인의 주문 수정")
    public ResponseEntity<String> updateOrder(@RequestPart(name = "image", required = false) MultipartFile image,
                                              @RequestPart(name = "request") @Valid CreateOrderRequestDTO createOrderRequestDTO,
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
