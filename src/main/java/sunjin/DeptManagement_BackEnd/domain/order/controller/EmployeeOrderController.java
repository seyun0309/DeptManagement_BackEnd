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
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.createOrderRequestDTO;
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
                                              @RequestPart(name = "request") @Valid createOrderRequestDTO createOrderRequestDTO){
        commonOrderService.createOrder(image, createOrderRequestDTO);
        return ResponseEntity.ok("주문에 성공했습니다.");
    }

    @GetMapping("/employee/{orderId}")
    @Operation(summary = "[사원] 주문 상세 조회", description = "신청한 주문의 상세 내역을 조회합니다")
    public ResponseEntity<GetOrderDetailResponseDTO> getOrder(@PathVariable("orderId") Long orderId) throws IOException {
        GetOrderDetailResponseDTO response = commonOrderService.getOrderDetails(orderId);
        return ResponseEntity.ok(response);
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

    @GetMapping("/employee/img/{orderId}")
    @Operation(summary = "[사원] 수정 버튼 클릭", description = "수정 버튼을 클릭하면 해당 주문의 사진을 리턴합니다")
    public ResponseEntity<Resource> getImg(@PathVariable("orderId") Long orderId) {
        try {
            Resource resource = commonOrderService.getImg(orderId);

            // 파일이 존재하고 읽을 수 있는 경우 리턴
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // 이미지 타입에 따라 적절히 변경
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (BusinessException | IOException e) {
            // BusinessException이 발생하면 예외 처리
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PatchMapping("/employee/{orderId}")
    @Operation(summary = "[사원] 주문 수정", description = "물품 타입, 물품 이름, 개당 가격, 수량를 입력하면 물품 수정이 진행됩니다")
    public ResponseEntity<String> updateOrder(@RequestPart(name = "image") MultipartFile image,
                                              @RequestPart(name = "request") @Valid createOrderRequestDTO createOrderRequestDTO,
                                              @PathVariable("orderId") Long orderId){
        commonOrderService.updateOrder(image, createOrderRequestDTO, orderId);
        return ResponseEntity.ok("주문 수정에 성공했습니다.");
    }

    @DeleteMapping("/employee/{orderId}")
    @Operation(summary = "[사원] 주문 삭제", description = "URL의 orderID를 통해 해당 물품 삭제가 진행됩니다.")
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId") Long orderId){
        commonOrderService.deleteOrder(orderId);
        return ResponseEntity.ok("주문 삭제에 성공했습니다.");
    }
}
