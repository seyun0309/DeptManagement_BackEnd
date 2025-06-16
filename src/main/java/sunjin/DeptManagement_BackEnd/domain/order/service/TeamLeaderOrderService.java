package sunjin.DeptManagement_BackEnd.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamLeaderOrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final AuthUtil authUtil;

    public void submitOrder(List<Long> ids) {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        authUtil.extractMemberAfterTokenValidation();

        // 2. ids를 통해서 Order 추출하고 상태를 "IN_SECOND_PROGRESS"로 변경한 뒤 재저장
        for(Long orderId : ids) {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
            order.submit(ApprovalStatus.IN_SECOND_PROGRESS, null, LocalDateTime.now());
            orderRepository.save(order);
        }
    }

    public DepartmentInfoResponseDTO getDepartmentInfo() {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        Member member = authUtil.extractMemberAfterTokenValidation();

        // 2. 로그인 사용자(팀장)의 부서와 그 부서에 속한 사원 정보 추출
        Long departmentId = member.getDepartment().getId();
        List<Role> role = Arrays.asList(Role.EMPLOYEE, Role.TEAMLEADER);
        List<Member> employeeList = memberRepository.findByDepartmentId(departmentId, role);

        if (employeeList.isEmpty()) {
            throw new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        // 3. 부서에 속한 사원 정보로 DTO 리스트 생성
        List<MemberResponseDTO> memberResponseDTOList = new ArrayList<>();

        for(Member m : employeeList) {
            MemberResponseDTO response = MemberResponseDTO.builder()
                    .memberId(m.getId())
                    .memberName(m.getUserName())
                    .build();

            memberResponseDTOList.add(response);
        }

        // 4. 부서 정보와 사원 정보 클라이언트에 리턴
        return DepartmentInfoResponseDTO.builder()
                .deptName(member.getDepartment().getDepartment().getDescription())
                .deptId(member.getDepartment().getId())
                .members(memberResponseDTOList)
                .build();
    }

    public List<?> getDepartmentDetails(Long memberId, List<String> statuses) {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        Member member = authUtil.extractMemberAfterTokenValidation();

        // 2. Order 담을 List 생성
        List<Order> orders;

        // 3. statuses가 들어온 경우 해당 상태로 변환, 없으면 모든 statuses로 처리
        List<ApprovalStatus> statusList = (statuses != null && !statuses.isEmpty())
                ? statuses.stream().map(ApprovalStatus::fromDescription).toList()
                : List.of(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS, ApprovalStatus.APPROVE, ApprovalStatus.DENIED);

        // 4. 조건별(memberId, statuses) 주문 목록 조회
        if(memberId != null && (statuses != null && !statuses.isEmpty())) {
            // Case 1: memberId, statuses가 모두 null이 아닌 경우
            orders = orderRepository.findByMemberIdAndStatusIn(memberId, statusList);
        } else if(memberId == null && (statuses != null && !statuses.isEmpty())) {
            // Case 2: memberId는 null이고 statuses는 null이 아닌 경우
            orders = orderRepository.findByDepartmentIdAndStatusIn(member.getDepartment().getId(), statusList);
        } else if(memberId != null) {
            // Case 3: memberId가 null이 아니고 statuses는 null인 경우
            orders = orderRepository.findByMemberIdAndStatusIn(memberId, statusList);
        } else {
            // Case 4: 모두 null인 경우
            orders = orderRepository.findByDepartmentIdAndStatusIn(member.getDepartment().getId(), statusList);
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
            String applicantDeptName = order.getDepartment() != null ? order.getDepartment().getDepartment().getDescription(): null;

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

    public List<FirstProgressOrdersResponseDTO> getFirstProgressOrders() {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        Member member = authUtil.extractMemberAfterTokenValidation();

        // 2. 사원이 팀장에게 상신한 목록 추출
        List<Order> orders = orderRepository.findByStatusIsFirstProgress(member.getDepartment().getId(), ApprovalStatus.IN_FIRST_PROGRESS);

        // 3. 리턴 타입으로 List 생성 및 초기화
        List<FirstProgressOrdersResponseDTO> progressOrderDTOList = new ArrayList<>();

        // 4. 리턴 타입 필드에 맞게 매핑
        for (Order order : orders) {
            // 시간 string으로 포맷
            String createDateFormatted = order.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));

            // 주문 종류, 주문 상태, 신청자, 부서 이름 string으로 포맷
            String productType = order.getOrderType() != null ? order.getOrderType().getDescription() : null;
            String orderStatus = "1차 처리중";
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
        return progressOrderDTOList;
    }

    @Transactional
    public void approveOrRejectOrderByTeamLeader(Long orderId, ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO) {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        Member member = authUtil.extractMemberAfterTokenValidation();

        // 2. orderId를 통해 Order 추출
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 3. 승인 / 반려 처리
        if(approveOrDeniedRequestDTO.getIsApproved().equals("true")) { // "승인"인 경우
            order.submit(ApprovalStatus.IN_SECOND_PROGRESS, LocalDateTime.now(), order.getSecondProcDate());
        } else { // "반려"인 경우
            order.denied(ApprovalStatus.DENIED, LocalDateTime.now(), order.getSecondProcDate(), approveOrDeniedRequestDTO.getDeniedDescription());
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
