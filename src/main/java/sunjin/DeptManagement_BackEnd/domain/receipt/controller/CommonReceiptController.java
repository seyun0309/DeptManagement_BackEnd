package sunjin.DeptManagement_BackEnd.domain.receipt.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sunjin.DeptManagement_BackEnd.domain.receipt.dto.request.RegisterReceiptRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.receipt.dto.response.DepartmentReceiptResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.receipt.repository.ReceiptRepository;
import sunjin.DeptManagement_BackEnd.domain.receipt.service.CommonReceiptService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CommonReceiptController {
    private final CommonReceiptService commonReceiptService;

    @PostMapping("/api/receipt")
    @Operation(summary = "[회원] 영수증 등록 로직", description = "영수증을 등록합니다. AI를 통해 인식한 텍스트를 사용자가 바꿀 수 있습니다.")
    public ResponseEntity<String> registerReceipt(@RequestPart(required = false, name = "image") MultipartFile image,
                                                  @RequestPart(name = "request") @Valid RegisterReceiptRequestDTO registerReceiptRequestDTO){
        commonReceiptService.registerReceipt(image, registerReceiptRequestDTO);
        return ResponseEntity.ok("영수증 등록에 성공했습니다");
    }

    @GetMapping("/api/receipt")
    @Operation(summary = "[회원] 본인 부서의 영수증 내역 조회",
            description = "현재 로그인한 사용자의 토큰을 통해 본인 부서의 모든 영수증 내역 조회가 진행됩니다")
    public ResponseEntity<List<DepartmentReceiptResponseDTO>> getAllReceipt(){
        List<DepartmentReceiptResponseDTO> response = commonReceiptService.getAllReceiptByDepartment();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/receipt/{receiptId}")
    @Operation(summary = "[회원] 본인 부서의 영수증 이미지 조회",
            description = "현재 로그인한 사용자의 토큰을 통해 해당 영수증의 이미지 조회가 진행됩니다")
    public String getReceiptImage(@PathVariable("receiptId") Long receiptId) {
        return commonReceiptService.getReceiptImg(receiptId);
    }
}
