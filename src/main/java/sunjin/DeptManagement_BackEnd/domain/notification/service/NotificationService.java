package sunjin.DeptManagement_BackEnd.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.domain.notification.domain.Notification;
import sunjin.DeptManagement_BackEnd.domain.notification.dto.response.GetNotificationsDTO;
import sunjin.DeptManagement_BackEnd.domain.notification.repository.NotificationRepository;
import sunjin.DeptManagement_BackEnd.global.auth.service.AuthUtil;
import sunjin.DeptManagement_BackEnd.global.enums.ErrorCode;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final AuthUtil authUtil;
    private final MemberRepository memberRepository;

    public void sendToUser(Long userId, String message) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // DB 저장
        Notification notification = Notification.builder()
                .message(message)
                .isRead(false)
                .receiver(member)
                .build();

        notificationRepository.save(notification);

        // 실시간 전송
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                message
        );
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
