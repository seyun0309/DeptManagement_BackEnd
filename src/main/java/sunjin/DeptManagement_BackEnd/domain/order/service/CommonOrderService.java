package sunjin.DeptManagement_BackEnd.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.createOrderRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.*;
import sunjin.DeptManagement_BackEnd.domain.order.repository.OrderRepository;
import sunjin.DeptManagement_BackEnd.global.auth.service.JwtProvider;
import sunjin.DeptManagement_BackEnd.global.enums.ApprovalStatus;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.enums.OrderType;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonOrderService {
    @Value("${image.upload.dir}")
    private String imageUploadDir;

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public void createOrder(MultipartFile image, createOrderRequestDTO createOrderRequestDTO) {
        // 현재 로그인 정보 확인
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if(member.getRefreshToken() != null){
            // 이미지 저장
            if (image.isEmpty()) {
                throw new BusinessException(ErrorCode.IMG_NOT_FOUND);
            }

            String storedFileName;
            Department department = member.getDepartment();
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
            String imgUrl = storedFileName;

            // ProductType 변환
            OrderType productType = createOrderRequestDTO.getProductTypeEnum();

            // 주문 매핑
            Order order = Order.builder()
                    .orderType(productType)
                    .storeName(createOrderRequestDTO.getStoreName())
                    .totalPrice(createOrderRequestDTO.getTotalPrice())
                    .description(createOrderRequestDTO.getDescription())
                    .status(ApprovalStatus.WAIT)
                    .rejectionDescription(null)
                    .receiptImgPath(receiptImgPath)
                    .ImgURL(imgUrl)
                    .firstProcDate(null)
                    .secondProcDate(null)
                    .member(member)
                    .department(department)
                    .build();

            // 주문 저장
            orderRepository.save(order);
        } else{
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    public GetOrderDetailResponseDTO getOrderDetails(Long orderId) throws IOException {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if(member.getRefreshToken() != null){
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

            String orderType = order.getOrderType() != null ? order.getOrderType().getDescription() : null;
            String createDateFormatted = order.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));
            String firstProcDateFormatted = order.getFirstProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));
            String secondProcDateFormmated = order.getSecondProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));
            Resource resource = getImg(orderId);

            return GetOrderDetailResponseDTO.builder()
                    .DeptName(member.getDepartment().getDeptName())
                    .applicantName(member.getUserName())
                    .orderType(orderType)
                    .storeName(order.getStoreName())
                    .totalPrice(order.getTotalPrice())
                    .description(order.getDescription())
                    .createdAt(createDateFormatted)
                    .firstProcDate(firstProcDateFormatted)
                    .secondProcDate(secondProcDateFormmated)
                    .rejectionDescription(order.getRejectionDescription())
                    .resource(ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG) // 이미지 타입에 따라 적절히 변경
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                            .body(resource).getBody())
                    .build();
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    public List<?> getOrders(String status) {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRefreshToken() != null) {
            List<Order> orders;
            if (status != null) {
                ApprovalStatus approvalStatus = ApprovalStatus.fromDescription(status);
                orders = orderRepository.findAllByMemberIdAndStatus(member.getId(), approvalStatus);
            } else {
                List<ApprovalStatus> progressStatuses = Arrays.asList(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS, ApprovalStatus.APPROVE, ApprovalStatus.DENIED);
                orders = orderRepository.findByMemberIdAndStatusIn(member.getId(), progressStatuses);
            }


            List<WaitOrdersResponseDTO> waitOrderDTOList = new ArrayList<>();
            List<ProgressOrdersResponseDTO> progressOrderDTOList = new ArrayList<>();
            List<DeniedOrdersResponseDTO> deniedOrderDTOList = new ArrayList<>();
            List<ApproveOrdersResponseDTO> approveOrderDTOList = new ArrayList<>();
            List<GetAllOrderDTO> getAllOrderDTOList = new ArrayList<>();

            for (Order order : orders) {
                // 시간 string으로 포맷
                String createDateFormatted = order.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));

                // 주문 종류, 주문 상태, 신청자, 부서 이름 string으로 포맷
                String productType = order.getOrderType() != null ? order.getOrderType().getDescription() : null;
                String orderStatus = null;
                if (order.getStatus() != null) {
                     if(order.getStatus() == ApprovalStatus.WAIT) {
                        orderStatus = "대기";
                    } else if(order.getStatus() == ApprovalStatus.DENIED) {
                        orderStatus = "반려";
                    } else if(order.getStatus() == ApprovalStatus.APPROVE) {
                        orderStatus = "승인";
                    } else if(order.getStatus() == ApprovalStatus.IN_FIRST_PROGRESS){
                        orderStatus = "1차 처리중";
                    } else {
                         orderStatus = "2차 처리중";
                     }
                }
                String applicantName = order.getMember() != null ? order.getMember().getUserName() : null;
                String applicantDeptName = order.getDepartment() != null ? order.getDepartment().getDeptName() : null;

                if ("wait".equalsIgnoreCase(status) && order.getStatus() == ApprovalStatus.WAIT) {
                    WaitOrdersResponseDTO waitOrderDTO = WaitOrdersResponseDTO.builder()
                            .orderId(order.getId())
                            .applicantDeptName(applicantDeptName)
                            .applicant(applicantName)
                            .productType(productType)
                            .storeName(order.getStoreName())
                            .totalPrice(order.getTotalPrice())
                            .description(order.getDescription())
                            .orderStatus(orderStatus)
                            .createdAt(createDateFormatted)
                            .build();
                    waitOrderDTOList.add(waitOrderDTO);
                } else if ("progress".equalsIgnoreCase(status) && (order.getStatus() == ApprovalStatus.IN_FIRST_PROGRESS || order.getStatus() == ApprovalStatus.IN_SECOND_PROGRESS)) {
                    ProgressOrdersResponseDTO progressOrderDTO = ProgressOrdersResponseDTO.builder()
                            .orderId(order.getId())
                            .applicantDeptName(applicantDeptName)
                            .applicant(applicantName)
                            .productType(productType)
                            .storeName(order.getStoreName())
                            .totalPrice(order.getTotalPrice())
                            .description(order.getDescription())
                            .orderStatus(orderStatus)
                            .createdAt(createDateFormatted)
                            .build();
                    progressOrderDTOList.add(progressOrderDTO);
                } else if ("denied".equalsIgnoreCase(status) && order.getStatus() == ApprovalStatus.DENIED) {
                    String procDate = order.getSecondProcDate() == null ? order.getFirstProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분")) : order.getSecondProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));
                    DeniedOrdersResponseDTO deniedOrderDTO = DeniedOrdersResponseDTO.builder()
                            .orderId(order.getId())
                            .applicantDeptName(applicantDeptName)
                            .applicant(applicantName)
                            .productType(productType)
                            .storeName(order.getStoreName())
                            .totalPrice(order.getTotalPrice())
                            .description(order.getDescription())
                            .orderStatus(orderStatus)
                            .deniedDescription(order.getRejectionDescription())
                            .createdAt(createDateFormatted)
                            .procDate(procDate)
                            .build();
                    deniedOrderDTOList.add(deniedOrderDTO);
                } else if ("approve".equalsIgnoreCase(status) && order.getStatus() == ApprovalStatus.APPROVE) {
                    String procDate = order.getSecondProcDate() == null ? order.getFirstProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분")) : order.getSecondProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));
                    ApproveOrdersResponseDTO approveOrderDTO = ApproveOrdersResponseDTO.builder()
                            .orderId(order.getId())
                            .applicantDeptName(applicantDeptName)
                            .applicant(applicantName)
                            .productType(productType)
                            .storeName(order.getStoreName())
                            .totalPrice(order.getTotalPrice())
                            .description(order.getDescription())
                            .orderStatus(orderStatus)
                            .createdAt(createDateFormatted)
                            .procDate(procDate)
                            .build();
                    approveOrderDTOList.add(approveOrderDTO);
                } else {
                    String procDate = (order.getSecondProcDate() != null)
                            ? order.getSecondProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"))
                            : (order.getFirstProcDate() != null)
                            ? order.getFirstProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"))
                            : "-";

                    GetAllOrderDTO getAllOrderDTO = GetAllOrderDTO.builder()
                            .orderId(order.getId())
                            .applicantDeptName(applicantDeptName)
                            .applicant(applicantName)
                            .productType(productType)
                            .storeName(order.getStoreName())
                            .totalPrice(order.getTotalPrice())
                            .description(order.getDescription())
                            .orderStatus(orderStatus)
                            .createdAt(createDateFormatted)
                            .procDate(procDate)
                            .build();
                    getAllOrderDTOList.add(getAllOrderDTO);
                }
            }

            if ("wait".equalsIgnoreCase(status)) {
                return waitOrderDTOList;
            } else if ("progress".equalsIgnoreCase(status)) {
                return progressOrderDTOList;
            } else if ("denied".equalsIgnoreCase(status)) {
                return deniedOrderDTOList;
            } else if ("approve".equalsIgnoreCase(status)) {
                return approveOrderDTOList;
            } else {
                return getAllOrderDTOList;
            }
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    public Resource getImg(Long orderId) throws IOException {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_APPLICANT));

        if (member.getRefreshToken() != null) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

            // 이미지 파일 경로 가져오기
            String imagePath = order.getReceiptImgPath();
            if (imagePath == null) {
                throw new BusinessException(ErrorCode.IMG_NOT_FOUND);
            }

            // 파일 경로를 리소스로 변환
            Path filePath = Paths.get(imagePath);
            Resource resource = new UrlResource(filePath.toUri());

            // 파일이 존재하고 읽을 수 있는 경우 리턴
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException(ErrorCode.IMG_NOT_FOUND);
            }
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    @Transactional
    public void updateOrder(MultipartFile image, createOrderRequestDTO createOrderRequestDTO, Long orderId) {
        //해당 주문 신청자와 현재 로그인한 사람 비교
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if(member.getRefreshToken() != null) {
            //주문 상태가 "대기"인 경우에만 수정 가능
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
            if (order.getStatus() != ApprovalStatus.WAIT) {
                throw new BusinessException(ErrorCode.ORDER_NOT_WAITING);
            }

            if (order.getDeletedAt() != null) {
                throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
            }

            if (!Objects.equals(member.getLoginId(), order.getMember().getLoginId())) {
                throw new BusinessException(ErrorCode.INVALID_APPLICANT);
            }

            // ProductType 변환
            OrderType productType = createOrderRequestDTO.getProductTypeEnum();

            // 이미지 저장
            if (image.isEmpty()) {
                throw new BusinessException(ErrorCode.IMG_NOT_FOUND);
            }

            String storedFileName;
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
            String imgUrl = storedFileName;

            order.updateInfo(
                    productType,
                    createOrderRequestDTO.getStoreName(),
                    createOrderRequestDTO.getTotalPrice(),
                    createOrderRequestDTO.getDescription(),
                    imgUrl,
                    receiptImgPath);

            orderRepository.save(order);
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_APPLICANT));

        if(member.getRefreshToken() != null) {
            //물품 상태가 "대기"인 경우에만 삭제 가능
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
            if (order.getStatus() != ApprovalStatus.WAIT) {
                throw new BusinessException(ErrorCode.ORDER_NOT_WAITING);
            }

            //해당 물품 신청자와 현재 로그인한 사람 비교
            if (!Objects.equals(member.getLoginId(), order.getMember().getLoginId())) {
                throw new BusinessException(ErrorCode.INVALID_APPLICANT);
            }

            order.setDeletedAt(LocalDateTime.now());
            orderRepository.save(order);
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }
}
