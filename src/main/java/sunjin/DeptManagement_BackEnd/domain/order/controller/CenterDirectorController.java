package sunjin.DeptManagement_BackEnd.domain.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.ApproveOrDeniedRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.DepartmentInfoResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.ProgressOrdersResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.service.CenterDirectorService;
import sunjin.DeptManagement_BackEnd.domain.order.service.CommonOrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/centerdirector")
public class CenterDirectorController implements CenterDirectorControllerDocs{
    private final CenterDirectorService centerDirectorService;
    private final CommonOrderService commonOrderService;

    @GetMapping("/img/{orderId}")
    public ResponseEntity<String> getImg(@PathVariable("orderId") Long orderId) {
        String response = commonOrderService.getImg(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/department")
    public ResponseEntity<List<DepartmentInfoResponseDTO>> getDepartment(){
        List<DepartmentInfoResponseDTO> departmentInfoResponseDTOS = centerDirectorService.getDepartmentInfo();
        return ResponseEntity.ok(departmentInfoResponseDTOS);
    }

    @GetMapping("/department/details")
    public ResponseEntity<List<?>> getDepartmentDetails(
            @RequestParam(value = "department", required = false) Long departmentId,
            @RequestParam(value = "member", required = false) Long memberId,
            @RequestParam(value = "status", required = false) List<String> statuses){
        List<?> response = centerDirectorService.getDepartmentDetails(departmentId, memberId, statuses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/department/progress")
    public ResponseEntity<List<ProgressOrdersResponseDTO>> getFirstProgressOrders() {
        List<ProgressOrdersResponseDTO> response = centerDirectorService.getSecondProgressOrders();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/department/submit/{orderId}")
    public ResponseEntity<String> approveOrRejectOrderByCenterDirector(@PathVariable("orderId") Long orderId, @Valid @RequestBody ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO) {
        centerDirectorService.approveOrRejectOrderByCenterDirector(orderId, approveOrDeniedRequestDTO);
        return ResponseEntity.ok("처리가 완료되었습니다.");
    }
}
