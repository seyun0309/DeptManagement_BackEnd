package sunjin.DeptManagement_BackEnd.global.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(400, "C001", "유효하지 않은 입력 값입니다"),
    METHOD_NOT_ALLOWED(405, "C002", "지원하지 않는 메서드입니다"),
    ENTITY_NOT_FOUND(404, "C003", "엔티티를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(500, "C004", "서버에서 에러가 발생했습니다"),
    INVALID_TYPE_VALUE(400, "C005", "유효하지 않은 형식의 값입니다"),
    HANDLE_ACCESS_DENIED(403, "C006", "접근 권한이 없습니다"),
    URL_NOT_FOUND(404, "C007", "요청한 주소의 API를 찾을 수 없습니다"),
    MISSING_PARAMETER(400, "C008", "필수 값인 매개변수를 찾을 수 없습니다"),
    DATA_INTEGRITY_VIOLATION(400, "C008", "잘못된 데이터에 접근하였습니다, 요청 값을 확인해주세요"),
    HTTP_MESSAGE_NOT_READABLE(400, "C009", "잘못된 JSON 요청 형식입니다"),
    ILLEGAL_ARGUMENT(400, "C010", "잘못된 인수 값이 포함된 요청입니다"),
    DATE_TIME_PARSE_FAILURE(400, "C011", "잘못된 DateTime 형식입니다"),
    HTTP_MESSAGE_CONVERSION(500, "C012", "요청 데이터 변환에 실패했습니다. 고객센터로 문의해주세요"),
    LOGIN_REQUIRED(401, "C013", "로그인 후에 해당 서비스를 이용해주세요"),

    // Member
    MEMBER_NOT_FOUND(404, "M001", "존재하지 않는 회원입니다"),
    MEMBER_PROFILE_DUPLICATION(400, "M002", "이미 존재하는 회원입니다"),
    INVALID_PASSWORD(404,"M003", " 잘못된 비밀번호 입니다"),
    INVALID_APPLICANT(404, "M004", "해당 주문을 신청한 사람이 아닙니다"),

    // Department
    DEPARTMENT_NOT_FOUND(404, "D001", "존재하는 부서가 없습니다"),

    // Order
    ORDER_NOT_FOUND(404, "O001", "존재하는 주문이 없습니다"),
    ORDER_NOT_WAITING(400, "O002", "주문이 대기중일 때만 가능합니다"),
    INVALID_STATUS(400, "O003", "올바른 주문 상태가 아닙니다."),

    // Receipt
    RECEIPT_NOT_FOUND(404, "R001", "존재하는 영수증이 없습니다"),
    IMG_NOT_FOUND(404, "R002", "이미지를 넣어주세요"),
    IMG_ONLY_JPG(404, "R003", "이미지의 파일을 JPG로 바꿔주세요"),

    // Token
    MISMATCH_REFRESH_TOKEN(401, "T001", "유효하지 않은 리프레시 토큰입니다"),
    NO_PERMISSION(401, "T002", "요청에 대한 권한이 없습니다"),
    EXPIRED_REFRESH_TOKEN(401, "T003", "유효시간이 만료된 리프레시 토큰입니다"),
    LOGGED_OUT_ACCESS_TOKEN(401, "T004", "로그아웃된 토큰입니다"),
    INVALID_ACCESS_TOKEN(401, "T005", "유효하지 않은 액세스 토큰입니다"),


    //S3(Image)
    EMPTY_FILE_EXCEPTION(400, "S001", "파일이 비어있습니다"),
    IO_EXCEPTION_ON_IMAGE_UPLOAD(500, "S002", "이미지 업로드 중에 IO 예외 발생"),
    NO_FILE_EXTENSION(400, "S003", "확장자가 없습니다"),
    INVALID_FILE_EXTENSION(400, "S004", "jpg, jpeg, png, gif가 아닌 확장자가 들어왔습니다"),
    PUT_OBJECT_EXCEPTION(500, "S005", "S3에 저장할 때 문제가 발생하였습니다"),
    IO_EXCEPTION_ON_IMAGE_DELETE(500, "S006", "파일을 삭제 중에 입출력 예외 발생"),

    ;

    private final String code;
    private final String message;
    private final int status;

    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }
}
