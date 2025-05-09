package sunjin.DeptManagement_BackEnd.domain.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
import sunjin.DeptManagement_BackEnd.domain.order.repository.OrderRepository;
import sunjin.DeptManagement_BackEnd.global.auth.service.AuthUtil;
import sunjin.DeptManagement_BackEnd.global.auth.service.JwtProvider;
import sunjin.DeptManagement_BackEnd.global.auth.service.RedisUtil;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.enums.ApprovalStatus;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeOrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final AuthUtil authUtil;

    @Transactional
    public void submitOrder(List<Long> ids) {
        Long currentUserId = authUtil.extractUserIdAfterTokenValidation();

        Member member = memberRepository.findById(currentUserId).orElseThrow(() -> new BusinessException(ErrorCode.INVALID_APPLICANT));

        if(member.getRefreshToken() != null) {
            for(Long orderId : ids) {
                Order order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
                order.submit(ApprovalStatus.IN_FIRST_PROGRESS, null, null);
                orderRepository.save(order);
            }
        } else {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
    }
}
