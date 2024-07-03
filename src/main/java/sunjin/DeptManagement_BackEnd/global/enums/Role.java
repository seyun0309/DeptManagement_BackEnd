package sunjin.DeptManagement_BackEnd.global.enums;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("admin", "관리자"),
    EMPLOYEE("employee", "사원");

    private final String code ;
    private final String description;

    Role(String code,  String description) {
        this.code = code;
        this.description = description;
    }
}
