package sunjin.DeptManagement_BackEnd.global.enums;

import lombok.Getter;

@Getter
public enum DeptType {
    DIGITAL_SCM("digital_scm","디지털 SCM"),
    DIGITAL_FCM("digital_fcm","디지털 FCM"),
    DATA_INNOVATION("data_innovation","데이터 이노베이션"),
    ACCOUNTANCY("accountancy", "회계");

    private final String code ;
    private final String description;

    DeptType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
