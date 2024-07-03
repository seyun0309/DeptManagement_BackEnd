package sunjin.DeptManagement_BackEnd.global.enums;

import lombok.Getter;

@Getter
public enum ProductType {
    FIXTURES("fixtures","비품"),
    SNACK("snack","간식"),
    ETC("etc", "기타");

    private final String code ;
    private final String description;

    ProductType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
