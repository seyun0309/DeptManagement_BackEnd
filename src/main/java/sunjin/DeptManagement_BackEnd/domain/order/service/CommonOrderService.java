package sunjin.DeptManagement_BackEnd.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.CreateOrderRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.*;
import sunjin.DeptManagement_BackEnd.domain.order.repository.OrderRepository;
import sunjin.DeptManagement_BackEnd.global.auth.service.AuthUtil;
import sunjin.DeptManagement_BackEnd.global.enums.ApprovalStatus;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.enums.OrderType;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonOrderService {
    private final OrderRepository orderRepository;
    private final S3ImageService s3ImageService;
    private final AuthUtil authUtil;

    @Transactional
    public void createOrder(MultipartFile image, CreateOrderRequestDTO createOrderRequestDTO) {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        Member member = authUtil.extractMemberAfterTokenValidation();

        // 2. 이미지 s3 저장 후 url 받아옴
        String imgUrl = null;
        if (image != null && !image.isEmpty()) {
            imgUrl = saveBoardImages(image);
        }

        // 3. 사용자 부서 정보 가져오기
        Department department = member.getDepartment();

        // 4. 주문 타입 가져오기
        OrderType productType = createOrderRequestDTO.getProductTypeEnum();

        // 5. 주문 객체 생성
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

        // 6. 주문 저장
        orderRepository.save(order);
    }

    public GetOrderDetailResponseDTO getOrderDetails(Long orderId) {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        Member member = authUtil.extractMemberAfterTokenValidation();

        // 2. orderId를 통해서 Order 추출
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 3. 주문 상세 DTO 생성 및 클라이언트에 리턴
        String orderType = order.getOrderType() != null ? order.getOrderType().getDescription() : null;
        String createDateFormatted = order.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));
        String firstProcDateFormatted = order.getFirstProcDate() != null ?order.getFirstProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분")) : "-";
        String secondProcDateFormatted = order.getSecondProcDate() != null ? order.getSecondProcDate().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분")) : "-";
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
                .secondProcDate(secondProcDateFormatted)
                .rejectionDescription(order.getRejectionDescription())
                .receiptImgUrl(receiptImgUrl)
                .build();
    }

    public List<?> getOrders(List<String> statuses) {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        Member member = authUtil.extractMemberAfterTokenValidation();

        // 2. statuses가 들어온 경우 해당 상태로 변환, 없으면 모든 statuses로 처리
        List<ApprovalStatus> statusList = (statuses != null && !statuses.isEmpty())
                ? statuses.stream().map(ApprovalStatus::fromDescription).toList()
                : List.of(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS, ApprovalStatus.APPROVE, ApprovalStatus.DENIED);

        // 3. DB에서 꺼내온 Order 리스트 초기화
        List<Order> orders = orderRepository.findByMemberIdAndStatusIn(member.getId(), statusList);

        // 4. DTO 리스트 초기화
        List<WaitOrdersResponseDTO> waitOrderDTOList = new ArrayList<>();
        List<FirstProgressOrdersResponseDTO> firstProgressOrderDTOList = new ArrayList<>();
        List<SecondProgressOrderResponseDTO> secondProgressOrderResponseDTOList = new ArrayList<>();
        List<DeniedOrdersResponseDTO> deniedOrderDTOList = new ArrayList<>();
        List<ApproveOrdersResponseDTO> approveOrderDTOList = new ArrayList<>();
        List<GetAllOrderDTO> getAllOrderDTOList = new ArrayList<>();

        // 5. 상태에 맞게 DTO 생성
        for (Order order : orders) {
            // 시간 string으로 포맷
            String createDateFormatted = order.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));

            // 주문 종류, 주문 상태, 신청자, 부서 이름 string으로 포맷
            String productType = order.getOrderType() != null ? order.getOrderType().getDescription() : null;

            String orderStatus = switch(order.getStatus()) {
                case WAIT -> "대기";
                case DENIED -> "반려";
                case APPROVE -> "승인";
                case IN_FIRST_PROGRESS -> "1차 처리중";
                case IN_SECOND_PROGRESS -> "2차 처리중";
            };

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
                    FirstProgressOrdersResponseDTO progressOrderDTO = FirstProgressOrdersResponseDTO.builder()
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
                    firstProgressOrderDTOList.add(progressOrderDTO);
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

        // 6. statuses에 따라 클라이언트에 리턴
        if (statuses == null || statuses.size() > 1) return getAllOrderDTOList;
        return switch (statuses.get(0).toLowerCase()) {
            case "wait" -> waitOrderDTOList;
            case "first" -> firstProgressOrderDTOList;
            case "second" -> secondProgressOrderResponseDTOList;
            case "denied" -> deniedOrderDTOList;
            default -> approveOrderDTOList;
        };
    }

    public String getImg(Long orderId) {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        authUtil.extractMemberAfterTokenValidation();

        // 2. orderId를 통해서 Order 추출
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 3. 해당 Order의 이미지 Url 리턴
        return order.getReceiptImgUrl();
    }

    @Transactional
    public void updateOrder(MultipartFile image, CreateOrderRequestDTO createOrderRequestDTO, Long orderId) {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        Member member = authUtil.extractMemberAfterTokenValidation();

        // 2. orderId를 통해서 Order 추출
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        /*
            주문 수정 조건
            1. 주문 상태가 "대기"인 경우
            2. 주문자와 로그인 사용자가 "동일"
            3. 삭제된 주문이면 안 됨
         */

        // 3. 주문 상태가 "대기"인 경우에만 수정 가능
        if (order.getStatus() != ApprovalStatus.WAIT) {
            throw new BusinessException(ErrorCode.ORDER_NOT_WAITING);
        }

        // 4. 주문자와 로그인 사용자가 "동일"한 경우에만 수정 가능
        if (!Objects.equals(member.getId(), order.getMember().getId())) {
            throw new BusinessException(ErrorCode.INVALID_APPLICANT);
        }

        // 5. 삭제되지 않은 주문인 경우에만 수정 가능
        if (order.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        String imgUrl = (image == null || image.isEmpty()) // 이미지 저장 (이미지 유무 확인)
                ? order.getReceiptImgUrl()
                : s3ImageService.upload(image);

        // 6. 주문 수정 메서드
        order.updateInfo(
                createOrderRequestDTO.getProductTypeEnum(),
                createOrderRequestDTO.getStoreName(),
                createOrderRequestDTO.getTotalPrice(),
                createOrderRequestDTO.getDescription(),
                imgUrl);

        // 7. 주문 재저장
        orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        Member member = authUtil.extractMemberAfterTokenValidation();

        /*
            주문 삭제 조건
            1. 주문 상태가 "대기"인 경우
            2. 주문자와 로그인 사용자가 "동일"
         */

        // 2. 주문 상태가 "대기"인 경우에만 삭제 가능
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getStatus() != ApprovalStatus.WAIT) {
            throw new BusinessException(ErrorCode.ORDER_NOT_WAITING);
        }

        // 3. 주문자와 로그인 사용자가 "동일"한 경우에만 삭제 가능
        if (!Objects.equals(member.getId(), order.getMember().getId())) {
            throw new BusinessException(ErrorCode.INVALID_APPLICANT);
        }

        // 4. 논리 삭제 진행 및 재저장
        order.setDeletedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    public String saveBoardImages(MultipartFile image) {
        // S3 이미지 저장 메서드
        return s3ImageService.upload(image);
    }
}
