package sunjin.DeptManagement_BackEnd.domain.order.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.department.repository.DepartmentRepository;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.createOrderRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.DepartmentOrdersResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.GetAllOrderDTO;
import sunjin.DeptManagement_BackEnd.domain.order.repository.OrderRepository;
import sunjin.DeptManagement_BackEnd.global.auth.service.JwtProvider;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.enums.ProductStatusType;
import sunjin.DeptManagement_BackEnd.global.enums.ProductType;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonOrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public void createOrder(createOrderRequestDTO createOrderRequestDTO) {
        // 현재 로그인 정보 확인
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Department department = member.getDepartment();

        // ProductType 변환
        ProductType productType = createOrderRequestDTO.getProductTypeEnum();

        // 주문 매핑
        Order order = Order.builder()
                .productType(productType)
                .price(createOrderRequestDTO.getPrice())
                .productName(createOrderRequestDTO.getProductName())
                .quantity(createOrderRequestDTO.getQuantity())
                .totalPrice(createOrderRequestDTO.getPrice() * createOrderRequestDTO.getQuantity())
                .status(ProductStatusType.WAIT)
                .processDate(null)
                .member(member)
                .department(department)
                .build();

        // 주문 저장
        orderRepository.save(order);
    }

    public DepartmentOrdersResponseDTO getAllOrdersByDepartment() {
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Long departmentId = member.getDepartment().getId();

        // 논리적 삭제된 주문은 거르고 가져옴
        List<Order> orders = orderRepository.findAllByDeletedAtIsNull(departmentId);
        List<GetAllOrderDTO> orderDTOList = new ArrayList<>();
        int totalAmount = 0;

        for (Order order : orders) {
            String createDateFormatted = order.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일"));
            String processDateFormatted = "-";
            if(order.getProcessDate() != null) {
                processDateFormatted = order.getProcessDate().format(DateTimeFormatter.ofPattern("M월 d일"));
            }
            String productTypeDescription = order.getProductType() != null ? order.getProductType().getDescription() : null;
            String applicantName = order.getMember() != null ? order.getMember().getUserName() : null;
            String applicantDeptName = order.getDepartment() != null ? order.getDepartment().getDeptName() : null;

            GetAllOrderDTO getAllOrderDTO = new GetAllOrderDTO(
                    order.getId(),
                    createDateFormatted,
                    productTypeDescription,
                    order.getProductName(),
                    order.getPrice(),
                    order.getQuantity(),
                    order.getTotalPrice(),
                    order.getStatus(),
                    processDateFormatted,
                    applicantName,
                    applicantDeptName
            );

            orderDTOList.add(getAllOrderDTO);
            if(order.getStatus() == ProductStatusType.WAIT) {
                totalAmount += order.getTotalPrice();
            }
        }

        return new DepartmentOrdersResponseDTO(orderDTOList, totalAmount);
    }

    @Transactional
    public void updateOrder(createOrderRequestDTO createOrderRequestDTO, Long orderId) {
        //물품 상태가 "대기"일 경우에만 수정 가능
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getStatus() != ProductStatusType.WAIT) {
            throw new BusinessException(ErrorCode.ORDER_NOT_WAITING);
        }

        //해당 물품 신청자와 현재 로그인한 사람 비교
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (!Objects.equals(member.getLoginId(), order.getMember().getLoginId())) {
            throw new BusinessException(ErrorCode.INVALID_APPLICANT);
        }

        // ProductType 변환
        ProductType productType = createOrderRequestDTO.getProductTypeEnum();

        order.updateInfo(productType,
                createOrderRequestDTO.getProductName(),
                createOrderRequestDTO.getPrice(),
                createOrderRequestDTO.getQuantity(),
                createOrderRequestDTO.getPrice() * createOrderRequestDTO.getQuantity());
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        //물품 상태가 "대기"일 경우에만 삭제 가능
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getStatus() != ProductStatusType.WAIT) {
            throw new BusinessException(ErrorCode.ORDER_NOT_WAITING);
        }

        //해당 물품 신청자와 현재 로그인한 사람 비교
        long currentUserId = jwtProvider.extractIdFromTokenInHeader();
        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_APPLICANT));
        if (!Objects.equals(member.getLoginId(), order.getMember().getLoginId())) {
            throw new BusinessException(ErrorCode.INVALID_APPLICANT);
        }

        order.setDeletedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}