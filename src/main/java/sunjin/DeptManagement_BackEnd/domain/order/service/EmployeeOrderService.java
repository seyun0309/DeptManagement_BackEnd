package sunjin.DeptManagement_BackEnd.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
import sunjin.DeptManagement_BackEnd.domain.order.repository.OrderRepository;
import sunjin.DeptManagement_BackEnd.global.auth.service.AuthUtil;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.enums.ApprovalStatus;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeOrderService {

    private final OrderRepository orderRepository;
    private final AuthUtil authUtil;

    @Transactional
    public void submitOrder(List<Long> ids) {

        // 1. 액세스 토큰 블랙리스트(로그인_로그아웃) / 유효성 검사
        authUtil.extractMemberAfterTokenValidation();

        // 2. ids를 하나씩 꺼내서 상신 처리(IN_FIRST_PROGRESS) 후 재저장
        for(Long orderId : ids) {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
            order.submit(ApprovalStatus.IN_FIRST_PROGRESS, null, null);
            orderRepository.save(order);
        }
    }
}
