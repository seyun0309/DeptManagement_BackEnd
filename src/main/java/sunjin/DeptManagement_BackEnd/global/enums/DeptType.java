package sunjin.DeptManagement_BackEnd.global.enums;

import lombok.Getter;
import sunjin.DeptManagement_BackEnd.global.error.exception.BusinessException;

@Getter
public enum DeptType {
    DIGITAL_SCM("0123","디지털 SCM"),
    DIGITAL_FCM("4812","디지털 FCM"),
    DATA_INNOVATION("5812","데이터 이노베이션"),
    ACCOUNTANCY("8231", "회계");

    private final String code ;
    private final String description;

    DeptType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DeptType formCode(String code) {
        for (DeptType type : DeptType.values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND);
    }

}
