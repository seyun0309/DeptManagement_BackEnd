package sunjin.DeptManagement_BackEnd.global.enums;

import lombok.Getter;

@Getter
public enum Role {
    EMPLOYEE("EMPLOYEE", "사원"),
    TEAMLEADER("TEAMLEADER", "팀장"),
    CENTERDIRECTOR("CENTERDIRECTOR", "센터장");

    private final String code ;
    private final String description;

    Role(String code,  String description) {
        this.code = code;
        this.description = description;
    }
}
