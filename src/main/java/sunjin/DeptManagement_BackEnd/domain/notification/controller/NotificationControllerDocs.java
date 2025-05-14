package sunjin.DeptManagement_BackEnd.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import sunjin.DeptManagement_BackEnd.domain.notification.dto.response.GetNotificationsDTO;

import java.util.List;

@Tag(name = "알림 API", description = "알림함 조회")
public interface NotificationControllerDocs {

    @Operation(summary = "알림함 내역 조회", description = "알림함에 있는 모든 알림들을 조회합니다(알림은 최대 6개만 보여줌)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "알림함 조회 성공", content = @Content(schema = @Schema(implementation = GetNotificationsDTO.class))),
            @ApiResponse(responseCode = "404", description = "유효하지 않는 액세스 토큰", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그아웃 된 토큰", content = @Content)
    })
    public ResponseEntity<List<GetNotificationsDTO>> getNotifications();
}
