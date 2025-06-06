package sunjin.DeptManagement_BackEnd.global.enums;

import lombok.Getter;

@Getter
public enum ApprovalStatus {
    WAIT("대기","wait"),
    IN_FIRST_PROGRESS("1차 처리", "first"),
    IN_SECOND_PROGRESS("2차 처리", "second"),
    DENIED("반려","denied"),
    APPROVE("승인","approve");

    private final String code ;
    private final String description;

    ApprovalStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ApprovalStatus fromDescription(String description) {
        for (ApprovalStatus type : ApprovalStatus.values()) {
            if (type.description.equalsIgnoreCase(description)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported product type description: " + description);
    }
}
