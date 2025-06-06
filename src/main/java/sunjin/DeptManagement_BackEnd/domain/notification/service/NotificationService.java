package sunjin.DeptManagement_BackEnd.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.domain.notification.domain.Notification;
import sunjin.DeptManagement_BackEnd.domain.notification.dto.response.GetNotificationsDTO;
import sunjin.DeptManagement_BackEnd.domain.notification.repository.NotificationRepository;
import sunjin.DeptManagement_BackEnd.global.auth.service.AuthUtil;
import sunjin.DeptManagement_BackEnd.global.config.websocket.SocketTextHandler;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ObjectMapper objectMapper;
    private final SocketTextHandler socketTextHandler;
    private final NotificationRepository notificationRepository;
    private final AuthUtil authUtil;
    private final MemberRepository memberRepository;

    @Transactional
    public void sendToUser(Long userId, String message) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // DB 저장
        Notification notification = Notification.builder()
                .message(message)
                .isRead(false)
                .receiver(member)
                .build();

        notificationRepository.save(notification);

        // 실시간 WebSocket 전송
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "message", message,
                    "createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            ));

            socketTextHandler.sendToUser(userId, json);
        } catch (IOException e) {
            log.error("WebSocket 전송 실패: userId={}, message={}", userId, message, e);
        }
    }

    public List<GetNotificationsDTO> getNotifications() {
        // 사용자 정보 가져오기
        Member member = authUtil.extractMemberAfterTokenValidation();

        // 사용자 정보로 알림 찾기
        List<Notification> notificationList = notificationRepository.findByReceiverId(member.getId());

        // DTO 매핑해서 리턴
        List<GetNotificationsDTO> response = new ArrayList<>();
        for(Notification notification : notificationList) {
           GetNotificationsDTO getNotificationsDTO = GetNotificationsDTO.builder()
                   .message(notification.getMessage())
                   .isRead(notification.getIsRead())
                   .createdAt(notification.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")))
                   .build();

           response.add(getNotificationsDTO);
        }

        // 알림 읽음 처리
        for (Notification notification : notificationList) {
            if (!notification.getIsRead()) {
                notification.read();
            }
        }

        // 최대 6개까지만 응답
        int limit = Math.min(response.size(), 6);
        return response.subList(0, limit);
    }
}
