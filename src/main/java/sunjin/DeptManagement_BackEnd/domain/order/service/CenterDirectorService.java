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

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        authUtil.extractMemberAfterTokenValidation();

        // 2. Order 담을 List 생성
        List<Order> orders;

        // 3. statuses가 들어온 경우 해당 상태로 변환, 없으면 모든 statuses로 처리
        List<ApprovalStatus> statusList = (statuses != null && !statuses.isEmpty())
                ? statuses.stream().map(ApprovalStatus::fromDescription).toList()
                : List.of(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS, ApprovalStatus.APPROVE, ApprovalStatus.DENIED);

        // 4. 조건별(departmentId, memberId, statuses) 주문 목록 조회
        if (departmentId != null && memberId != null && (statuses != null && !statuses.isEmpty())) {
            // Case 1: demartmentId, memberId, statuses가 모두 null이 아닌 경우
            orders = orderRepository.findByDepartmentIdAndMemberAndStatusIn(departmentId, memberId, statusList);
        } else if (departmentId != null && memberId != null) {
            // Case 2: statuses만 null인 경우
            orders = orderRepository.findByDepartmentIdAndMemberAndStatusIn(departmentId, memberId, statusList);
        } else if (departmentId != null && (statuses != null && !statuses.isEmpty())) {
            // Case 3: memberId만 null인 경우
            orders = orderRepository.findByDepartmentIdAndStatusIn(departmentId, statusList);
        } else if (departmentId != null) {
            // Case 4: departmentId만 null이 아닌 경우
            orders = orderRepository.findByDepartmentIdAndStatusIn(departmentId, statusList);
        } else if (memberId != null && (statuses != null && !statuses.isEmpty())) {
            // Case 5: demartmentId만 null인 경우
            orders = orderRepository.findByMemberIdAndStatusIn(memberId, statusList);
        } else if (memberId != null) {
            // Case 6: memberId만 null이 아닌 경우
            orders = orderRepository.findByMemberIdAndStatusIn(memberId, statusList);
        } else if ((statuses != null && !statuses.isEmpty())) {
            // Case 7: statuses만 null아닌 경우
            orders = orderRepository.findByStatusIn(statusList);
        } else {
            // Case 8: 모두 null인 경우
            orders = orderRepository.findByStatusIn(statusList);
        }

        // 5. DTO List 생성
        List<WaitOrdersResponseDTO> waitOrderDTOList = new ArrayList<>();
        List<FirstProgressOrdersResponseDTO> firstProgressOrderDTOList = new ArrayList<>();
        List<SecondProgressOrderResponseDTO> secondProgressOrderResponseDTOList = new ArrayList<>();
        List<DeniedOrdersResponseDTO> deniedOrderDTOList = new ArrayList<>();
        List<ApproveOrdersResponseDTO> approveOrderDTOList = new ArrayList<>();
        List<GetAllOrderDTO> getAllOrderDTOList = new ArrayList<>();

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

    public List<FirstProgressOrdersResponseDTO> getSecondProgressOrders() {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        authUtil.extractMemberAfterTokenValidation();

        // 2. "2차 처리 중"인 Order List 추출
        List<Order> orders = orderRepository.findByStatusIsSecondProgress(ApprovalStatus.IN_SECOND_PROGRESS);

        // 3. 리턴 타입으로 List 생성 및 초기화
        List<FirstProgressOrdersResponseDTO> progressOrderDTOList = new ArrayList<>();

        // 4. 리턴 타입 필드에 맞게 매핑
        for (Order order : orders) {
            // 시간 string으로 포맷
            String createDateFormatted = order.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));

            // 주문 종류, 주문 상태, 신청자, 부서 이름 string으로 포맷
            String productType = order.getOrderType() != null ? order.getOrderType().getDescription() : null;
            String orderStatus = "2차 처리중";
            String applicantName = order.getMember() != null ? order.getMember().getUserName() : null;
            String applicantDeptName = order.getDepartment() != null ? order.getDepartment().getDepartment().getDescription() : null;

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
            progressOrderDTOList.add(progressOrderDTO);
        }

        // 5. 클라이언트에 리턴
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
