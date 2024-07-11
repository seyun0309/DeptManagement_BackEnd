package sunjin.DeptManagement_BackEnd.domain.receipt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.domain.receipt.domain.Receipt;
import sunjin.DeptManagement_BackEnd.domain.receipt.dto.request.RegisterReceiptRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.receipt.dto.response.DepartmentReceiptResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.receipt.repository.ReceiptRepository;
import sunjin.DeptManagement_BackEnd.global.auth.service.JwtProvider;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonReceiptService {
    @Value("${image.upload.dir}")
    private String imageUploadDir;

    private final ReceiptRepository receiptRepository;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public String registerReceipt(MultipartFile image, RegisterReceiptRequestDTO registerReceiptRequestDTO) {
        // 현재 사용자 정보
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String storedFileName;
        if (member.getRefreshToken() != null) {
            if (image.isEmpty()) {
                throw new BusinessException(ErrorCode.IMG_NOT_FOUND);
            }

            Department department = member.getDepartment();

            String storeName = registerReceiptRequestDTO.getStoreName();
            String receiptPrice = registerReceiptRequestDTO.getReceiptPrice();

            // 파일 저장 로직
            String originalFilename = image.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            storedFileName = UUID.randomUUID().toString() + extension;
            File dest = new File(imageUploadDir + "/" + storedFileName);

            try {
                image.transferTo(dest);
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패", e);
            }

            String receiptImgPath = imageUploadDir + "/" + storedFileName;
            String imgUrl = "http://localhost:8080/api/receipt/image/" + storedFileName;

            Receipt receipt = Receipt.builder()
                    .receiptImgPath(receiptImgPath)
                    .storeName(storeName)
                    .receiptPrice(Integer.parseInt(receiptPrice))
                    .recDate(LocalDateTime.now())
                    .member(member)
                    .department(department)
                    .ImgURL(imgUrl)
                    .build();

            receiptRepository.save(receipt);
            return storedFileName;
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    public List<DepartmentReceiptResponseDTO> getAllReceiptByDepartment() {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if(member.getRefreshToken()!=null) {
            Long departmentId = member.getDepartment().getId();
            List<Receipt> receipts = receiptRepository.findAllById(departmentId);
            List<DepartmentReceiptResponseDTO> departmentReceiptResponseDTOList = new ArrayList<>();

            for(Receipt receipt : receipts) {
                String registerDateFormmet = "-";
                if(receipt.getCreatedAt() != null) {
                    registerDateFormmet = receipt.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일"));
                }

                DepartmentReceiptResponseDTO departmentReceiptResponseDTO = new DepartmentReceiptResponseDTO(
                        receipt.getId(),
                        registerDateFormmet,
                        receipt.getStoreName(),
                        receipt.getReceiptPrice()
                );
                departmentReceiptResponseDTOList.add(departmentReceiptResponseDTO);
            }
            return departmentReceiptResponseDTOList;
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    public String getReceiptImg(Long receiptId) {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if(member.getRefreshToken()!=null) {
            Receipt receipt = receiptRepository.findById(receiptId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RECEIPT_NOT_FOUND));

            return receipt.getImgURL();
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }
}
