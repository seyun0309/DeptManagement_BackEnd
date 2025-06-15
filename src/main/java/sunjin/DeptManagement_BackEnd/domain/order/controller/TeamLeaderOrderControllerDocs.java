package sunjin.DeptManagement_BackEnd.domain.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.ApproveOrDeniedRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.request.CreateOrderRequestDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.DepartmentInfoResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.GetAllOrderDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.GetOrderDetailResponseDTO;
import sunjin.DeptManagement_BackEnd.domain.order.dto.response.FirstProgressOrdersResponseDTO;

import java.util.List;

@Tag(name = "팀장 API", description = "주문 신청, 상세 조회, 상신, 본인 주문 상태별 조회, (드롭다운) 부서명, 사원명 조회, 사원 주문 상태별 조회, 본인에게 상싱된 목록 조회, 승인/반려 처리, (수정모달) 이미지 리턴, 주문 수정, 주문 삭제")
public interface TeamLeaderOrderControllerDocs {
    @Operation(summary = "[팀장] 주문 신청")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "주문 성공", content = @Content),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그아웃된 토큰", content = @Content)
    })
    @Parameter(name = "image", description = "영수증 이미지", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
    public ResponseEntity<String> createOrder(@RequestPart(required = false, name = "image") MultipartFile image,
                                              @RequestPart(name = "request") @Valid CreateOrderRequestDTO createOrderRequestDTO);


    @Operation(summary = "[팀장] 본인의 주문 상세 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = GetAllOrderDTO.class))),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그아웃된 토큰", content = @Content)
    })
    @Parameter(name = "orderId", description = "URL에 포함된 주문 ID", in = ParameterIn.PATH)
    public ResponseEntity<GetOrderDetailResponseDTO> getOrder(@PathVariable("orderId") Long orderId);

    @Operation(summary = "[팀장] 본인의 주문을 센터장에게 상신")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "상신 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그아웃된 토큰", content = @Content)

    })
    @Parameter(name = "order", description = "쿼리 스트링에 포함된 주문 ID", in = ParameterIn.QUERY)
    public ResponseEntity<String> submitOrder(@RequestParam(value = "order", required = false) List<Long> ids);

    @Operation(summary = "[팀장] 본인의 주문을 전체 및 상태별 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = GetAllOrderDTO.class))),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그아웃된 토큰", content = @Content)
    })
    @Parameter(name = "stats", description = "쿼리 스트링에 포함된 주문 상태", in = ParameterIn.QUERY)
    public ResponseEntity<List<?>> getAllOrder(@RequestParam(value = "status", required = false) List<String> statuses);

    @Operation(summary = "[팀장] 부서 이름, 사원 이름 리턴 -> 드롭다운에 적용", description = "조회 버튼을 클릭하면 부서 이름, 사원 이름을 리턴합니다")
    public ResponseEntity<DepartmentInfoResponseDTO> getDepartment();

    @Operation(summary = "[팀장] 사원명, 상태를 적절히 골라 조회 진행 후 DTO 리턴")
    public ResponseEntity<List<?>> getDepartmentDetails(
            @RequestParam(value = "member", required = false) Long memberId,
            @RequestParam(value = "status", required = false) List<String> statuses);

    @Operation(summary = "[팀장] 사원이 팀장에게 상신한 목록들 가져옴")
    public ResponseEntity<List<FirstProgressOrdersResponseDTO>> getFirstProgressOrders();

    @Operation(summary = "[팀장] 사원의 주문을 승인/반려 처리함")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "처리 성공 및 실시간 알림 전송", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주문", content = @Content)

    })
    @Parameter(name = "orderId", description = "URL에 포함된 주문 ID", in = ParameterIn.PATH)
    public ResponseEntity<String> approveOrRejectOrderByTeamLeader(@PathVariable("orderId") Long orderId, @RequestBody ApproveOrDeniedRequestDTO approveOrDeniedRequestDTO);

    @Operation(summary = "[팀장] 수정 모달에 띄울 이미지 리턴", description = "수정 버튼을 클릭하면 해당 주문의 사진을 리턴합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그아웃된 토큰", content = @Content)
    })
    @Parameter(name = "orderId", description = "URL에 포함된 주문 ID", in = ParameterIn.PATH)
    public ResponseEntity<String> getImg(@PathVariable("orderId") Long orderId);

    @Operation(summary = "[팀장] 본인의 주문 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "대기 상태가 아닌 주문", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주문 또는 유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "401", description = "주문자 불일치 또는 로그아웃된 토큰", content = @Content)
    })
    @Parameters(value = {
            @Parameter(name = "image", description = "영수증 이미지", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)),
            @Parameter(name = "orderId", description = "URL에 포함된 주문 ID", in = ParameterIn.PATH)
    })
    public ResponseEntity<String> updateOrder(@RequestPart(name = "image") MultipartFile image,
                                              @RequestPart(name = "request") @Valid CreateOrderRequestDTO createOrderRequestDTO,
                                              @PathVariable("orderId") Long orderId);


    @Operation(summary = "[팀장] 본인의 주문 삭제", description = "URL의 orderID를 통해 해당 물품 삭제가 진행됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "대기 상태가 아닌 주문", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 주문 또는 유효하지 않은 토큰", content = @Content),
            @ApiResponse(responseCode = "401", description = "주문자 불일치 또는 로그아웃된 토큰", content = @Content)
    })
    @Parameter(name = "orderId", description = "URL에 포함된 주문 ID", in = ParameterIn.PATH)
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId") Long orderId);
}
