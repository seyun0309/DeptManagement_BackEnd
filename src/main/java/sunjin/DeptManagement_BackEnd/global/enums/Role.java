package sunjin.DeptManagement_BackEnd.global.enums;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("ADMIN", "관리자"),
    EMPLOYEE("EMPLOYEE", "사원");

    private final String code ;
    private final String description;

    Role(String code,  String description) {
        this.code = code;
        this.description = description;
    }
}
