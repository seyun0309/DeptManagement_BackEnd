package sunjin.DeptManagement_BackEnd.global.enums;

import lombok.Getter;

@Getter
public enum ApprovalStatus {
    WAIT("wait","wait"),
    IN_FIRST_PROGRESS("in_first_progress", "first"),
    IN_SECOND_PROGRESS("in_second_progress", "second"),
    DENIED("denied","denied"),
    APPROVE("approve","approve");

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
