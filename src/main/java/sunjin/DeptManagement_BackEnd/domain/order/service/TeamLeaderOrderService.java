package sunjin.DeptManagement_BackEnd.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.ApproveOrDeniedRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.*;
import sunjin.DeptManagement_BackEnd.domain.order.repository.OrderRepository;
import sunjin.DeptManagement_BackEnd.global.auth.service.JwtProvider;
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
    private final JwtProvider jwtProvider;

    public void submitOrder(List<Long> ids) {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_APPLICANT));

        if(member.getRefreshToken() != null) {
            for(Long orderId : ids) {
                Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
                order.submit(ApprovalStatus.IN_SECOND_PROGRESS, null, LocalDateTime.now());
                orderRepository.save(order);
            }
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    public DepartmentInfoResponseDTO getDepartmentInfo() {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_APPLICANT));

        Long departmentId = member.getDepartment().getId();
        List<Role> role = Arrays.asList(Role.EMPLOYEE, Role.TEAMLEADER);
        List<Member> employeeList = memberRepository.findByDepartmentId(departmentId, role);

        if (employeeList.isEmpty()) {
            throw new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        List<MemberResponseDTO> memberResponseDTOList = employeeList.stream()
                .map(emp -> MemberResponseDTO.builder()
                        .memberId(emp.getId())
                        .memberName(emp.getUserName())
                        .build())
                .collect(Collectors.toList());

        return DepartmentInfoResponseDTO.builder()
                .deptName(member.getDepartment().getDeptName())
                .deptId(member.getDepartment().getId())
                .members(memberResponseDTOList)
                .build();
    }

    public List<?> getDepartmentDetails(Long memberId, List<String> statuses) {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if(member.getRefreshToken() != null) {
            List<Order> orders;
            if(memberId != null && (statuses != null && !statuses.isEmpty())) {
                List<ApprovalStatus> approvalStatuses = new ArrayList<>();
                for (String status : statuses) {
                    approvalStatuses.add(ApprovalStatus.fromDescription(status));
                }
                orders = orderRepository.findAllByMemberIdAndStatus(memberId, approvalStatuses);
            } else if(memberId == null && (statuses != null && !statuses.isEmpty())) {
                List<ApprovalStatus> approvalStatuses = new ArrayList<>();
                for (String status : statuses) {
                    approvalStatuses.add(ApprovalStatus.fromDescription(status));
                }
                orders = orderRepository.findByDepartmentIdAndStatus(member.getDepartment().getId(), approvalStatuses);
            } else if(memberId != null) {
                List<ApprovalStatus> progressStatuses = Arrays.asList(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS, ApprovalStatus.APPROVE, ApprovalStatus.DENIED);
                orders = orderRepository.findAllByMemberIdAndStatusIn(memberId, progressStatuses);
            } else {
                List<ApprovalStatus> progressStatuses = Arrays.asList(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS, ApprovalStatus.APPROVE, ApprovalStatus.DENIED);
                orders = orderRepository.findByDepartmentIdAndStatusIn(member.getDepartment().getId(), progressStatuses);
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

                if(statuses.size() > 1) {
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
                    } else if ("progress".equalsIgnoreCase(statuses.toString()) && (order.getStatus() == ApprovalStatus.IN_FIRST_PROGRESS || order.getStatus() == ApprovalStatus.IN_SECOND_PROGRESS)) {
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
                    } else if ("denied".equalsIgnoreCase(statuses.toString()) && order.getStatus() == ApprovalStatus.DENIED) {
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
                    } else if ("approve".equalsIgnoreCase(statuses.toString()) && order.getStatus() == ApprovalStatus.APPROVE) {
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

            if (statuses.size() > 1) {
                return getAllOrderDTOList;
            } else if ("wait".equalsIgnoreCase(statuses.toString())) {
                return waitOrderDTOList;
            } else if ("progress".equalsIgnoreCase(statuses.toString())) {
                return progressOrderDTOList;
            } else if ("denied".equalsIgnoreCase(statuses.toString())) {
                return deniedOrderDTOList;
            } else {
                return approveOrderDTOList;
            }
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    public List<ProgressOrdersResponseDTO> getFirstProgressOrders() {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRefreshToken() != null) {
            List<Order> orders = orderRepository.findByStatusIsProgress(ApprovalStatus.IN_FIRST_PROGRESS);
            List<ProgressOrdersResponseDTO> progressOrderDTOList = new ArrayList<>();

            for (Order order : orders) {
                // 시간 string으로 포맷
                String createDateFormatted = order.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));

                // 주문 종류, 주문 상태, 신청자, 부서 이름 string으로 포맷
                String productType = order.getOrderType() != null ? order.getOrderType().getDescription() : null;
                String orderStatus = "1차 처리중";
                String applicantName = order.getMember() != null ? order.getMember().getUserName() : null;
                String applicantDeptName = order.getDepartment() != null ? order.getDepartment().getDeptName() : null;

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
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }

    @Transactional
    public void approveOrRejectOrderByTeamLeader(Long orderId, ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO) {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRefreshToken() != null) {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
            if(approveOrDeniedRequestDTO.getIsApproved().equals("true")) {
                order.submit(ApprovalStatus.IN_SECOND_PROGRESS, LocalDateTime.now(), order.getSecondProcDate());
            } else {
                order.denied(ApprovalStatus.DENIED, LocalDateTime.now(), order.getSecondProcDate(), approveOrDeniedRequestDTO.getDeniedDescription());
            }
            orderRepository.save(order);
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }
}
