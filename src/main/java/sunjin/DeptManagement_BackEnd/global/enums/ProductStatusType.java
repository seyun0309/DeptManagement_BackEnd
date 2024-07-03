package sunjin.DeptManagement_BackEnd.global.enums;

import lombok.Getter;

@Getter
public enum ProductStatusType {
    WAIT("wait","대기"),
    DENIED("denied","반려"),
    APPROVE("approve","승인");

    private final String code ;
    private final String description;

    ProductStatusType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
