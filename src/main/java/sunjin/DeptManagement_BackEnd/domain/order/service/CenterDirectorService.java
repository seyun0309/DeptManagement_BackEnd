package sunjin.DeptManagement_BackEnd.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.department.repository.DepartmentRepository;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.domain.notification.service.NotificationService;
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.ApproveOrDeniedRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.*;
import sunjin.DeptManagement_BackEnd.domain.order.repository.OrderRepository;
import sunjin.DeptManagement_BackEnd.global.auth.service.AuthUtil;
import sunjin.DeptManagement_BackEnd.global.enums.ApprovalStatus;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.enums.Role;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CenterDirectorService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;
    private final AuthUtil authUtil;

    public List<DepartmentInfoResponseDTO> getDepartmentInfo() {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        authUtil.extractMemberAfterTokenValidation();

        // 2. 모든 부서 리스트 추출
        List<Department> allDepartment = departmentRepository.findAll();

        // 3. 리턴타입에 맞는 List 생성
        List<DepartmentInfoResponseDTO> departmentInfoList = new ArrayList<>();

        // 4. 추출했던 부서 List를 리턴 타입에 맞게 변경
        for (Department department : allDepartment) {
            String deptName = department.getDepartment().getDescription();
            Long deptId = department.getId();

            // 해당 부서에 속한 사원들 리스트 추출
            List<Member> memberListByDeptId = memberRepository.findByDepartmentId(department.getId());
            List<MemberResponseDTO> memberInfoList = new ArrayList<>();

            for (Member member1 : memberListByDeptId) {
                if(member1.getRole() != Role.CENTERDIRECTOR) {
                    MemberResponseDTO memberInfoDTO = MemberResponseDTO.builder()
                            .memberId(member1.getId())
                            .memberName(member1.getUserName())
                            .build();
                    memberInfoList.add(memberInfoDTO);
                }
            }

            // 5. 부서와 사원들을 묶어서 DTO 생성
            DepartmentInfoResponseDTO departmentInfoDTO = DepartmentInfoResponseDTO.builder()
                    .deptName(deptName)
                    .deptId(deptId)
                    .members(memberInfoList)
                    .build();

            departmentInfoList.add(departmentInfoDTO);
        }

        // 5. 클라이언트에 리턴
        return departmentInfoList;
    }
    public List<?> getDepartmentDetails(Long departmentId, Long memberId, List<String> statuses) {
        authUtil.extractMemberAfterTokenValidation();

        List<Order> orders;
        if (departmentId != null && memberId != null && (statuses != null && !statuses.isEmpty())) {
            // Case 1: departmentId, memberId, and status are all not null
            List<ApprovalStatus> approvalStatuses = new ArrayList<>();
            for (String status : statuses) {
                approvalStatuses.add(ApprovalStatus.fromDescription(status));
            }
            orders = orderRepository.findByDepartmentIdAndMemberAndStatusIn(departmentId, memberId, approvalStatuses);
        } else if (departmentId != null && memberId != null) {
            // Case 2: departmentId and memberId are not null, status is null
            List<ApprovalStatus> progressStatuses = Arrays.asList(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS, ApprovalStatus.APPROVE, ApprovalStatus.DENIED);
            orders = orderRepository.findByDepartmentIdAndMemberAndStatusIn(departmentId, memberId, progressStatuses);
        } else if (departmentId != null && (statuses != null && !statuses.isEmpty())) {
            // Case 3: departmentId is not null, memberId is null, status is not null
            List<ApprovalStatus> approvalStatuses = new ArrayList<>();
            for (String status : statuses) {
                approvalStatuses.add(ApprovalStatus.fromDescription(status));
            }
            orders = orderRepository.findByDepartmentIdAndStatusIn(departmentId, approvalStatuses);
        } else if (departmentId != null) {
            // Case 4: departmentId is not null, memberId and status are null
            List<ApprovalStatus> progressStatuses = Arrays.asList(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS, ApprovalStatus.APPROVE, ApprovalStatus.DENIED);
            orders = orderRepository.findByDepartmentIdAndStatusIn(departmentId, progressStatuses);
        } else if (memberId != null && (statuses != null && !statuses.isEmpty())) {
            // Case 5: departmentId is null, memberId and status are not null
            List<ApprovalStatus> approvalStatuses = new ArrayList<>();
            for (String status : statuses) {
                approvalStatuses.add(ApprovalStatus.fromDescription(status));
            }
            orders = orderRepository.findByMemberIdAndStatusIn(memberId, approvalStatuses);
        } else if (memberId != null) {
            // Case 6: departmentId is null, memberId is not null, status is null
            List<ApprovalStatus> progressStatuses = Arrays.asList(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS, ApprovalStatus.APPROVE, ApprovalStatus.DENIED);
            orders = orderRepository.findByMemberIdAndStatusIn(memberId, progressStatuses);
        } else if ((statuses != null && !statuses.isEmpty())) {
            // Case 7: departmentId and memberId are null, status is not null
            List<ApprovalStatus> approvalStatuses = new ArrayList<>();
            for (String status : statuses) {
                approvalStatuses.add(ApprovalStatus.fromDescription(status));
            }
            orders = orderRepository.findByStatusIn(approvalStatuses);
        } else {
            // Case 8: departmentId, memberId, and status are all null
            List<ApprovalStatus> progressStatuses = Arrays.asList(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS, ApprovalStatus.APPROVE, ApprovalStatus.DENIED);
            orders = orderRepository.findByStatusIn(progressStatuses);
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
            }  else {
                if ("wait".equalsIgnoreCase(statuses.toString()) && order.getStatus() == ApprovalStatus.WAIT) {
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
                } else if ("denied".equalsIgnoreCase(statuses.get(0)) && (order.getStatus() == ApprovalStatus.DENIED)) {
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
    }

    public List<ProgressOrdersResponseDTO> getSecondProgressOrders() {
        authUtil.extractMemberAfterTokenValidation();

        List<Order> orders = orderRepository.findByStatusIsSecondProgress(ApprovalStatus.IN_SECOND_PROGRESS);
        List<ProgressOrdersResponseDTO> progressOrderDTOList = new ArrayList<>();

        for (Order order : orders) {
            // 시간 string으로 포맷
            String createDateFormatted = order.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));

            // 주문 종류, 주문 상태, 신청자, 부서 이름 string으로 포맷
            String productType = order.getOrderType() != null ? order.getOrderType().getDescription() : null;
            String orderStatus = "2차 처리중";
            String applicantName = order.getMember() != null ? order.getMember().getUserName() : null;
            String applicantDeptName = order.getDepartment() != null ? order.getDepartment().getDepartment().getDescription() : null;

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
        }
        return progressOrderDTOList;
    }

    @Transactional
    public void approveOrRejectOrderByCenterDirector(Long orderId, ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO) {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        Member member = authUtil.extractMemberAfterTokenValidation();

        // 2. orderId를 통해 Order 추출
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 3. 승인 / 반려 처리
        if(approveOrDeniedRequestDTO.getIsApproved().equals("true")) { // "승인"인 경우
            order.submit(ApprovalStatus.APPROVE, order.getFirstProcDate(), LocalDateTime.now());
        } else { // "반려"인 경우
            order.denied(ApprovalStatus.DENIED, order.getFirstProcDate(), LocalDateTime.now(), approveOrDeniedRequestDTO.getDeniedDescription());
        }
        orderRepository.save(order);

        // 4. 주문자에게 보낼 알림 메세지 생성
        String message = String.format(
                "[%s] %s(%s)님에 의해 상태가 '%s'로 변경되었습니다.",
                order.getStoreName(),
                member.getUserName(),
                member.getRole().getDescription(),
                order.getStatus().getCode()
        );

        // 5. 실시간 알림 전송
        notificationService.sendToUser(order.getMember().getId(), message);
    }
}
