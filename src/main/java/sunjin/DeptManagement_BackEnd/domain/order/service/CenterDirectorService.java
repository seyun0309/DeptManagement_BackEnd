package sunjin.DeptManagement_BackEnd.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.department.repository.DepartmentRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class CenterDirectorService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final JwtProvider jwtProvider;

    public List<DepartmentInfoResponseDTO> getDepartmentInfo() {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_APPLICANT));

        if (member.getRefreshToken() != null) {
            List<Department> allDepartment = departmentRepository.findAll();
            List<DepartmentInfoResponseDTO> departmentInfoList = new ArrayList<>();

            for (Department department : allDepartment) {
                String deptName = department.getDeptName();
                Long deptId = department.getId();
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

                DepartmentInfoResponseDTO departmentInfoDTO = DepartmentInfoResponseDTO.builder()
                        .deptName(deptName)
                        .deptId(deptId)
                        .members(memberInfoList)
                        .build();
                departmentInfoList.add(departmentInfoDTO);
            }
            return departmentInfoList;
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }
    public List<?> getDepartmentDetails(Long departmentId, Long memberId, List<String> statuses) {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_APPLICANT));

        if (member.getRefreshToken() != null) {
            List<Order> orders;
            if (departmentId != null && memberId != null && (statuses != null && !statuses.isEmpty())) {
                // Case 1: departmentId, memberId, and status are all not null
                List<ApprovalStatus> approvalStatuses = new ArrayList<>();
                for (String status : statuses) {
                    approvalStatuses.add(ApprovalStatus.fromDescription(status));
                }
                orders = orderRepository.findByDepartmentIdAndMemberAndStatus(departmentId, memberId, approvalStatuses);
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
                orders = orderRepository.findByDepartmentIdAndStatus(departmentId, approvalStatuses);
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
                orders = orderRepository.findAllByMemberIdAndStatus(memberId, approvalStatuses);
            } else if (memberId != null) {
                // Case 6: departmentId is null, memberId is not null, status is null
                List<ApprovalStatus> progressStatuses = Arrays.asList(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS, ApprovalStatus.APPROVE, ApprovalStatus.DENIED);
                orders = orderRepository.findAllByMemberIdAndStatusIn(memberId, progressStatuses);
            } else if ((statuses != null && !statuses.isEmpty())) {
                // Case 7: departmentId and memberId are null, status is not null
                List<ApprovalStatus> approvalStatuses = new ArrayList<>();
                for (String status : statuses) {
                    approvalStatuses.add(ApprovalStatus.fromDescription(status));
                }
                orders = orderRepository.findByStatus(approvalStatuses);
            } else {
                // Case 8: departmentId, memberId, and status are all null
                List<ApprovalStatus> progressStatuses = Arrays.asList(ApprovalStatus.IN_FIRST_PROGRESS, ApprovalStatus.IN_SECOND_PROGRESS, ApprovalStatus.APPROVE, ApprovalStatus.DENIED);
                orders = orderRepository.findByStatusIn(progressStatuses);
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

            if (statuses == null || statuses.size() > 1) {
                return getAllOrderDTOList;
            } else if ("wait".equalsIgnoreCase(statuses.toString())) {
                return waitOrderDTOList;
            } else if ("first".equalsIgnoreCase(statuses.get(0))) {
                return progressOrderDTOList;
            } else if ("second".equalsIgnoreCase(statuses.get(0))) {
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

    public List<ProgressOrdersResponseDTO> getSecondProgressOrders() {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRefreshToken() != null) {
            List<Order> orders = orderRepository.findByStatusIsSecondProgress(ApprovalStatus.IN_SECOND_PROGRESS);
            List<ProgressOrdersResponseDTO> progressOrderDTOList = new ArrayList<>();

            for (Order order : orders) {
                // 시간 string으로 포맷
                String createDateFormatted = order.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일 H시 m분"));

                // 주문 종류, 주문 상태, 신청자, 부서 이름 string으로 포맷
                String productType = order.getOrderType() != null ? order.getOrderType().getDescription() : null;
                String orderStatus = "2차 처리중";
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
    public void approveOrRejectOrderByCenterDirector(Long orderId, ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO) {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getRefreshToken() != null) {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
            if(approveOrDeniedRequestDTO.getIsApproved().equals("true")) {
                order.submit(ApprovalStatus.APPROVE, order.getFirstProcDate(), LocalDateTime.now());
            } else {
                order.denied(ApprovalStatus.DENIED, order.getFirstProcDate(), LocalDateTime.now(), approveOrDeniedRequestDTO.getDeniedDescription());
            }
            orderRepository.save(order);
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }
}
