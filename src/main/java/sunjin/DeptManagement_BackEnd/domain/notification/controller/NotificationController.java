package sunjin.DeptManagement_BackEnd.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sunjin.DeptManagement_BackEnd.domain.notification.dto.response.GetNotificationsDTO;
import sunjin.DeptManagement_BackEnd.domain.notification.service.NotificationService;

import java.util.List;

@Tag(name = "알림", description = "알림함 조회")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping()
    @Operation(summary = "알림함 내역 조회", description = "알림함에 있는 모든 알림들을 조회합니다(알림은 최대 6개만 보여줌)")
    public ResponseEntity<List<GetNotificationsDTO>> getNotifications() {
        List<GetNotificationsDTO> response = notificationService.getNotifications();
        return ResponseEntity.ok(response);
    }
}
