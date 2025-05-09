package sunjin.DeptManagement_BackEnd.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.notification.domain.Notification;
import sunjin.DeptManagement_BackEnd.domain.notification.dto.response.GetNotificationsDTO;
import sunjin.DeptManagement_BackEnd.domain.notification.repository.NotificationRepository;
import sunjin.DeptManagement_BackEnd.global.auth.service.JwtProvider;
import sunjin.DeptManagement_BackEnd.global.auth.service.RedisUtil;
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
    private final JwtProvider jwtProvider;
    private final RedisUtil redisUtil;

    public void sendToUser(Long userId, String message) {
        // DB 저장
        Notification notification = Notification.builder()
                .message(message)
                .isRead(false)
                .receiver(Member.builder().id(userId).build())
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
        Long userId = extractUserIdAfterTokenValidation();

        // 사용자 정보로 알림 찾기
        List<Notification> notificationList = notificationRepository.findByReceiverId(userId);

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


    public Long extractUserIdAfterTokenValidation() {
        String token = jwtProvider.extractIdFromTokenInHeader();

        String status = redisUtil.getData(token);
        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN); // 등록되지 않은 토큰 (ex. 위조/만료/비정상 발급 등)
        }

        if ("logout".equals(status)) {
            throw new BusinessException(ErrorCode.LOGGED_OUT_ACCESS_TOKEN); //리프래시 토큰을 사용해서 액세스 토큰 다시 발급받기
        }

        return jwtProvider.extractIdFromToken(token);
    }
}
