package sunjin.DeptManagement_BackEnd.domain.order.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.global.common.BaseEntity;
import sunjin.DeptManagement_BackEnd.global.enums.ProductStatusType;
import sunjin.DeptManagement_BackEnd.global.enums.ProductType;

import java.time.LocalDateTime;

@SuperBuilder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @Column(nullable = false)
    private int price;

    @Column(length = 100, nullable = false)
    private String productName;

    @Min(value = 1)
    @Max(value = 100)
    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatusType status;

    @Column
    private LocalDateTime processDate;

    @JoinColumn(columnDefinition = "varchar(100)",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "department_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;

    public void updateInfo(ProductType productType, String productName, int price, int quantity, int totalPrice) {
        this.productType = productType;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public void updateStatus(ProductStatusType orderStatus, LocalDateTime localDateTime) {
        this.status = orderStatus;
        this.processDate = localDateTime;
    }
}
