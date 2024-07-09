package sunjin.DeptManagement_BackEnd.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sunjin.DeptManagement_BackEnd.domain.department.repository.DepartmentRepository;
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.UpdateStatusOrderRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.DepartmentOrdersResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.GetAllOrderDTO;
import sunjin.DeptManagement_BackEnd.domain.order.repository.OrderRepository;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.enums.ProductStatusType;
import sunjin.DeptManagement_BackEnd.global.enums.ProductType;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final DepartmentRepository departmentRepository;

    public List<GetAllOrderDTO> findAllOrder() {
        List<Order> orders = orderRepository.findAllByDeletedIsNull();

        List<GetAllOrderDTO> orderDTOList = new ArrayList<>();
        for (Order order : orders) {
            String createDateFormatted = order.getCreatedAt().format(DateTimeFormatter.ofPattern("M월 d일"));
            String processDateFormatted = "-";
            if(order.getProcessDate() != null) {
                processDateFormatted = order.getProcessDate().format(DateTimeFormatter.ofPattern("M월 d일"));
            }
            String productTypeDescription = order.getProductType() != null ? order.getProductType().getDescription() : null;
            String orderTypeStatus = order.getStatus() != null ? order.getStatus().getDescription() : null;
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
                    orderTypeStatus,
                    processDateFormatted,
                    applicantName,
                    applicantDeptName
            );
            orderDTOList.add(getAllOrderDTO);
        }
        return orderDTOList;
    }

    public DepartmentOrdersResponseDTO getAllOrdersByDepartment(Long departmentId) {
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));

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
            String orderTypeStatus = order.getStatus() != null ? order.getStatus().getDescription() : null;
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
                    orderTypeStatus,
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
    public void updateOrderStatus(UpdateStatusOrderRequestDTO updateStatusOrderRequestDTO, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if(order.getStatus() == ProductStatusType.WAIT){
            // ProductStatusType 변환
            ProductStatusType productStatusType = updateStatusOrderRequestDTO.getProductTypeEnum();
            order.updateStatus(productStatusType, LocalDateTime.now());
        } else {
            throw new BusinessException(ErrorCode.ORDER_NOT_WAITING);
        }
    }
}
