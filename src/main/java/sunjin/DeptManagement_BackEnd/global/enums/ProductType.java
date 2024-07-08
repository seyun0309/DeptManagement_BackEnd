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

    public static ProductType fromDescription(String description) {
        for (ProductType type : ProductType.values()) {
            if (type.description.equalsIgnoreCase(description)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported product type description: " + description);
    }
}
