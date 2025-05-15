package sunjin.DeptManagement_BackEnd.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.ApproveOrDeniedRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.DepartmentInfoResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.GetAllOrderDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.ProgressOrdersResponseDTO;

import java.util.List;

@Tag(name = "센터장 API", description = "(수정 모달)이미지 조회, (드롭 다운)부서/사원 정보 조회, 진행 중 주문 조회, 주문 승인/반려 처리")
public interface CenterDirectorControllerDocs {

    @Operation(summary = "[센터장] 수정 모달에 띄울 이미지 리턴", description = "수정 버튼을 클릭하면 해당 주문의 사진을 리턴합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "이미지 리턴 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "주문자 아님", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하는 주문 아님", content = @Content),

    })
    @Parameter(name = "orderId", description = "URL에 포함된 주문 ID", required = true, in = ParameterIn.PATH)
    public ResponseEntity<String> getImg(@PathVariable("orderId") Long orderId);

    @Operation(summary = "[센터장] 부서명, 사원명을 리턴 -> 드롭다운에 적용")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "부서명, 사원명 리턴 성공", content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = DepartmentInfoResponseDTO.class))
            )),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content),
    })
    public ResponseEntity<List<DepartmentInfoResponseDTO>> getDepartment();

    @Operation(summary = "[센터장] 부서명, 사원명, 상태를 적절히 골라 조회 진행 후 DTO 리턴")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetAllOrderDTO.class)))),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그아웃된 토큰", content = @Content)
    })
    @Parameters(value = {
            @Parameter(name = "department", description = "쿼리 스트링에 포함된 부서 ID", in = ParameterIn.QUERY),
            @Parameter(name = "member", description = "쿼리 스트링에 포함된 멤버 ID", in = ParameterIn.QUERY),
            @Parameter(name = "status", description = "쿼리 스트링에 포함된 주문 상태", in = ParameterIn.QUERY)
    })
    public ResponseEntity<List<?>> getDepartmentDetails(
            @RequestParam(value = "department", required = false) Long departmentId,
            @RequestParam(value = "member", required = false) Long memberId,
            @RequestParam(value = "status", required = false) List<String> statuses);

    @Operation(summary = "[센터장] 팀장이 사원의 주문을 승인한 것과 팀장이 센터장에게 상신한 목록들 가져옴")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "조회 성공", content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = ProgressOrdersResponseDTO.class))
            )),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content)
    })
    public ResponseEntity<List<ProgressOrdersResponseDTO>> getFirstProgressOrders();

    @Operation(summary = "[센터장] 직원의 주문 승인/반려 처리")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "처리 성공 및 실시간 알림 전송", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주문", content = @Content)

    })
    @Parameter(name = "orderId", description = "URL에 포함된 주문 ID", in = ParameterIn.PATH)
    public ResponseEntity<String> approveOrRejectOrderByTeamLeader(@PathVariable("orderId") Long orderId, @RequestBody ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO);
}
