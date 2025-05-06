package sunjin.DeptManagement_BackEnd.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.CreateOrderRequestDTO;
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
    private final S3ImageService s3ImageService;

    @Transactional
    public void createOrder(MultipartFile image, CreateOrderRequestDTO createOrderRequestDTO) {
        // 현재 로그인 정보 확인
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if(member.getRefreshToken() != null) {
            // 이미지 저장
            String imgUrl = null;
            if (image != null && !image.isEmpty()) {
                imgUrl = saveBoardImages(image);
            }

            Department department = member.getDepartment();

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
                    .receiptImgUrl(imgUrl)
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
            String secondProcDateFormated = order.getSecondProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));
            String receiptImgUrl = order.getReceiptImgUrl();

            return GetOrderDetailResponseDTO.builder()
                    .DeptName(member.getDepartment().getDepartment().getDescription())
                    .applicantName(member.getUserName())
                    .orderType(orderType)
                    .storeName(order.getStoreName())
                    .totalPrice(order.getTotalPrice())
                    .description(order.getDescription())
                    .createdAt(createDateFormatted)
                    .firstProcDate(firstProcDateFormatted)
                    .secondProcDate(secondProcDateFormated)
                    .rejectionDescription(order.getRejectionDescription())
                    .receiptImgUrl(receiptImgUrl)
                    .build();
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    public List<?> getOrders(List<String> statuses) {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRefreshToken() != null) {
            List<Order> orders;
            List<ApprovalStatus> approvalStatuses = new ArrayList<>();
            if(statuses != null && !statuses.isEmpty()) {
                for (String status : statuses) {
                    approvalStatuses.add(ApprovalStatus.fromDescription(status));
                }
                orders = orderRepository.findByMemberIdAndStatusIn(member.getId(), approvalStatuses);
            }  else {
                List<String> statusString = List.of("first", "second", "approve", "denied");
                for (String status : statusString) {
                    approvalStatuses.add(ApprovalStatus.fromDescription(status));
                }
                orders = orderRepository.findByMemberIdAndStatusIn(member.getId(), approvalStatuses);
            }


            List<WaitOrdersResponseDTO> waitOrderDTOList = new ArrayList<>();
            List<ProgressOrdersResponseDTO> progressOrderDTOList = new ArrayList<>();
            List<SecondProgressOrderResponseDTO> secondProgressOrderResponseDTOList = new ArrayList<>();
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
                String applicantDeptName = order.getDepartment() != null ? order.getDepartment().getDepartment().getDescription() : null;
                if(statuses == null || statuses.size() > 1) {
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
                            .deniedDescription(order.getRejectionDescription())
                            .createdAt(createDateFormatted)
                            .procDate(procDate)
                            .build();
                    getAllOrderDTOList.add(getAllOrderDTO);
                } else {
                    if ("wait".equalsIgnoreCase(statuses.get(0)) && order.getStatus() == ApprovalStatus.WAIT) {
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
                    } else if ("first".equalsIgnoreCase(statuses.get(0)) && (order.getStatus() == ApprovalStatus.IN_FIRST_PROGRESS)) {
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
                    } else if ("second".equalsIgnoreCase(statuses.get(0)) && (order.getStatus() == ApprovalStatus.IN_SECOND_PROGRESS)) {
                        String procDate = order.getSecondProcDate() == null ? order.getFirstProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분")) : order.getSecondProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));
                        SecondProgressOrderResponseDTO secondProgressOrderResponseDTO = SecondProgressOrderResponseDTO.builder()
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
                        secondProgressOrderResponseDTOList.add(secondProgressOrderResponseDTO);
                    } else if ("denied".equalsIgnoreCase(statuses.get(0)) && order.getStatus() == ApprovalStatus.DENIED) {
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
                    } else if ("approve".equalsIgnoreCase(statuses.get(0)) && order.getStatus() == ApprovalStatus.APPROVE) {
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
                    }
                }
            }

            if (statuses == null || statuses.size() > 1) {
                return getAllOrderDTOList;
            } else if ("wait".equalsIgnoreCase(statuses.get(0))) {
                return waitOrderDTOList;
            } else if ("first".equalsIgnoreCase(statuses.get(0))) {
                return progressOrderDTOList;
            } else if ("second".equalsIgnoreCase(statuses.get(0))) {
                return secondProgressOrderResponseDTOList;
            } else if ("denied".equalsIgnoreCase(statuses.get(0))) {
                return deniedOrderDTOList;
            } else {
                return approveOrderDTOList;
            }
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    public String getImg(Long orderId) {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_APPLICANT));

        if (member.getRefreshToken() != null) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

            return order.getReceiptImgUrl();
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    @Transactional
    public void updateOrder(MultipartFile image, CreateOrderRequestDTO createOrderRequestDTO, Long orderId) {
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
            String imgUrl = s3ImageService.upload(image);

            order.updateInfo(
                    productType,
                    createOrderRequestDTO.getStoreName(),
                    createOrderRequestDTO.getTotalPrice(),
                    createOrderRequestDTO.getDescription(),
                    imgUrl);

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

    public String saveBoardImages(MultipartFile image) {
        return s3ImageService.upload(image);
    }
}
