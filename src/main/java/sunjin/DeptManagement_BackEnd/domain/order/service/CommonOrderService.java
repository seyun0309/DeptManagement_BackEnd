package sunjin.DeptManagement_BackEnd.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

        String storedFileName;
        if(member.getRefreshToken() != null){
            // 이미지 저장
            if (image.isEmpty()) {
                throw new BusinessException(ErrorCode.IMG_NOT_FOUND);
            }

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
            String imgUrl = "http://localhost:8080/api/receipt/image/" + storedFileName;

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



    public List<?> getOrders(String status) {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRefreshToken() != null) {
            List<Order> orders;
            if (status != null) {
                if ("progress".equalsIgnoreCase(status)) {
                    List<ApprovalStatus> progressStatuses = Arrays.asList(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS);
                    orders = orderRepository.findByMemberIdAndStatusIn(member.getId(), progressStatuses);
                } else {
                    ApprovalStatus approvalStatus = ApprovalStatus.fromDescription(status);
                    orders = orderRepository.findAllByMemberIdAndStatus(member.getId(), approvalStatus);
                }
            } else {
                orders = orderRepository.findAllByMemberId(member.getId());
            }


            List<WaitOrdersResponseDTO> waitOrderDTOList = new ArrayList<>();
            List<ProgressOrdersResponseDTO> progressOrderDTOList = new ArrayList<>();
            List<DeniedOrdersResponseDTO> deniedOrderDTOList = new ArrayList<>();
            List<ApproveOrdersResponseDTO> approveOrderDTOList = new ArrayList<>();
            List<GetAllOrderDTO> getAllOrderDTOList = new ArrayList<>();

            for (Order order : orders) {
                // 시간 string으로 포맷
                String createDateFormatted = order.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));
                String modifiedDateFormmet = "-";
                if (order.getModifiedAt() != null) {
                    modifiedDateFormmet = order.getModifiedAt().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));
                }

                // 주문 종류, 주문 상태, 신청자, 부서 이름 string으로 포맷
                String productType = order.getOrderType() != null ? order.getOrderType().getDescription() : null;
                String orderStatus = null;
                if (order.getStatus() != null) {
                    if (order.getStatus() == ApprovalStatus.IN_FIRST_PROGRESS || order.getStatus() == ApprovalStatus.IN_SECOND_PROGRESS) {
                        orderStatus = "처리중";
                    } else {
                        orderStatus = order.getStatus().getDescription();
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
                            .updatedAt(modifiedDateFormmet)
                            .build();
                    deniedOrderDTOList.add(deniedOrderDTO);
                } else if ("approve".equalsIgnoreCase(status) && order.getStatus() == ApprovalStatus.APPROVE) {
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
                            .updatedAt(modifiedDateFormmet)
                            .build();
                    approveOrderDTOList.add(approveOrderDTO);
                } else {
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
                            .updatedAt(modifiedDateFormmet)
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

    @Transactional
    public void updateOrder(createOrderRequestDTO createOrderRequestDTO, Long orderId) {
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

            order.updateInfo(
                    productType,
                    createOrderRequestDTO.getStoreName(),
                    createOrderRequestDTO.getTotalPrice(),
                    createOrderRequestDTO.getDescription());
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
