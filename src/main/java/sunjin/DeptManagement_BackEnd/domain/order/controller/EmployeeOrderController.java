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
public class EmployeeOrderController implements EmployeeOrderControllerDocs{
    private final CommonOrderService commonOrderService;
    private final EmployeeOrderService employeeOrderService;

    @PostMapping("/employee/orders")
    public ResponseEntity<String> createOrder(@RequestPart(name = "image") MultipartFile image,
                                              @RequestPart(name = "request") @Valid CreateOrderRequestDTO createOrderRequestDTO){
        commonOrderService.createOrder(image, createOrderRequestDTO);
        return ResponseEntity.ok("주문에 성공했습니다.");
    }

    @GetMapping("/employee/{orderId}")
    public ResponseEntity<GetOrderDetailResponseDTO> getOrder(@PathVariable("orderId") Long orderId) {
        GetOrderDetailResponseDTO response = commonOrderService.getOrderDetails(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/submit")
    public ResponseEntity<String> submitOrder(@RequestParam(value = "order", required = false) List<Long> ids){
        employeeOrderService.submitOrder(ids);
        return ResponseEntity.ok("상신에 성공했습니다.");
    }

    @GetMapping("/employee/orders")
    public ResponseEntity<List<?>> getAllOrder(@RequestParam(value = "status", required = false) List<String> statuses) {
        List<?> response = commonOrderService.getOrders(statuses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/img/{orderId}")
    public ResponseEntity<String> getImg(@PathVariable("orderId") Long orderId) {
        String response = commonOrderService.getImg(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/employee/{orderId}")
    public ResponseEntity<String> updateOrder(@RequestPart(name = "image") MultipartFile image,
                                              @RequestPart(name = "request") @Valid CreateOrderRequestDTO createOrderRequestDTO,
                                              @PathVariable("orderId") Long orderId){
        commonOrderService.updateOrder(image, createOrderRequestDTO, orderId);
        return ResponseEntity.ok("주문 수정에 성공했습니다.");
    }

    @DeleteMapping("/employee/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId") Long orderId){
        commonOrderService.deleteOrder(orderId);
        return ResponseEntity.ok("주문 삭제에 성공했습니다.");
    }
}
