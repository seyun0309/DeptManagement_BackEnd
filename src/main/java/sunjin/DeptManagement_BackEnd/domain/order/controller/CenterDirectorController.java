package sunjin.DeptManagement_BackEnd.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.ApproveOrDeniedRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.DepartmentInfoResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.ProgressOrdersResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.service.CenterDirectorService;
import sunjin.DeptManagement_BackEnd.domain.order.service.CommonOrderService;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CenterDirectorController {
    private final CenterDirectorService centerDirectorService;
    private final CommonOrderService commonOrderService;

    @GetMapping("/centerdirector/img/{orderId}")
    @Operation(summary = "[팀장] 수정 모달에 띄울 이미지 리턴", description = "수정 버튼을 클릭하면 해당 주문의 사진을 리턴합니다")
    public ResponseEntity<String> getImg(@PathVariable("orderId") Long orderId) {
        String response = commonOrderService.getImg(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/centerdirector/department")
    @Operation(summary = "[센터장] 부서 이름, 사원 이름을 리턴 -> 드롭다운에 적용")
    public ResponseEntity<List<DepartmentInfoResponseDTO>> getDepartment(){
        List<DepartmentInfoResponseDTO> departmentInfoResponseDTOS = centerDirectorService.getDepartmentInfo();
        return ResponseEntity.ok(departmentInfoResponseDTOS);
    }

    @GetMapping("/centerdirector/department/details")
    @Operation(summary = "[센터장] 부서명, 사원명, 상태를 적절히 골라 조회 진행 후 DTO 리턴")
    public ResponseEntity<List<?>> getDepartmentDetails(
            @RequestParam(value = "department", required = false) Long departmentId,
            @RequestParam(value = "member", required = false) Long memberId,
            @RequestParam(value = "status", required = false) List<String> statuses){
        List<?> response = centerDirectorService.getDepartmentDetails(departmentId, memberId, statuses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/centerdirector/department/progress")
    @Operation(summary = "[센터장] 팀장이 사원의 주문을 승인한 것과 팀장이 센터장에게 상신한 목록들 가져옴")
    public ResponseEntity<List<ProgressOrdersResponseDTO>> getFirstProgressOrders() {
        List<ProgressOrdersResponseDTO> response = centerDirectorService.getSecondProgressOrders();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/centerdirector/department/submit/{orderId}")
    @Operation(summary = "[센터장] 직원의 주문 승인/반려 처리")
    public ResponseEntity<String> approveOrRejectOrderByTeamLeader(@PathVariable("orderId") Long orderId, @RequestBody ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO) {
        centerDirectorService.approveOrRejectOrderByCenterDirector(orderId, approveOrDeniedRequestDTO);
        return ResponseEntity.ok("처리가 완료되었습니다.");
    }
}
