package sunjin.DeptManagement_BackEnd.global.enums;

import lombok.Getter;

@Getter
public enum OrderType {
    FIXTURES("fixtures","비품"),
    SNACK("snack","간식"),
    FOOD_COSTS("food", "식비"),
    GENERAL("general", "일반 경비"),
    ENTERTAINMENT("entertainment", "접대비"),
    TRANSPORTATION("transportation", "교통비"),
    ETC("etc", "기타");

    private final String code ;
    private final String description;

    OrderType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderType fromDescription(String description) {
        for (OrderType type : OrderType.values()) {
            if (type.description.equalsIgnoreCase(description)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported product type description: " + description);
    }
}
