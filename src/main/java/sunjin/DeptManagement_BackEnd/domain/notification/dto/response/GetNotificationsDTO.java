package sunjin.DeptManagement_BackEnd.domain.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GetNotificationsDTO {
    private String message;
    private boolean isRead;
    private String createdAt;
}
