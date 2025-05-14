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

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/notifications")
public class NotificationController implements NotificationControllerDocs{
    private final NotificationService notificationService;

    @GetMapping()
    public ResponseEntity<List<GetNotificationsDTO>> getNotifications() {
        List<GetNotificationsDTO> response = notificationService.getNotifications();
        return ResponseEntity.ok(response);
    }
}
