package sunjin.DeptManagement_BackEnd.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.ApproveOrDeniedRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.DepartmentInfoResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.ProgressOrdersResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.service.CenterDirectorService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CenterDirectorController {
    private final CenterDirectorService centerDirectorService;

    //TODO 부서 이름, 사원 이름 리턴
    @GetMapping("/centerdirector/department")
    @Operation(summary = "[센터장] 부서 이름, 사원 이름을 리턴 -> 드롭다운에 적용")
    public ResponseEntity<List<DepartmentInfoResponseDTO>> getDepartment(){
        List<DepartmentInfoResponseDTO> departmentInfoResponseDTOS = centerDirectorService.getDepartmentInfo();
        return ResponseEntity.ok(departmentInfoResponseDTOS);
    }

    //TODO 각종 조회(전체, 부서, 이름, 현황...)
    @GetMapping("/centerdirector/department/details")
    @Operation(summary = "[센터장] 부서명, 사원명, 상태를 적절히 골라 조회 진행 후 DTO 리턴")
    public ResponseEntity<List<?>> getDepartmentDetails(
            @RequestParam(value = "department", required = false) String departmentId,
            @RequestParam(value = "member", required = false) String memberId,
            @RequestParam(value = "status", required = false) String status){
        List<?> response = centerDirectorService.getDepartmentDetails(Long.parseLong(departmentId), Long.parseLong(memberId), status);
        return ResponseEntity.ok(response);
    }

    //TODO 승인/반려 버튼 클릭 -> 2차 처리중 주문 리턴
    @GetMapping("/centerdirector/department/progress")
    @Operation(summary = "[센터장] 팀장이 사원의 주문을 승인한 것과 팀장이 센터장에게 상신한 목록들 가져옴")
    public ResponseEntity<List<ProgressOrdersResponseDTO>> getFirstProgressOrders() {
        List<ProgressOrdersResponseDTO> response = centerDirectorService.getSecondProgressOrders();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/centerdirector/department/submit/{orderId}")
    @Operation(summary = "[센터장] 직원의 주문 승인/반려 처리")
    public ResponseEntity<String> approveOrRejectOrderByTeamLeader(@PathVariable("orderId") Long orderId, ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO) {
        centerDirectorService.approveOrRejectOrderByCenterDirector(orderId, approveOrDeniedRequestDTO);
        return ResponseEntity.ok("처리가 완료되었습니다.");
    }
}
