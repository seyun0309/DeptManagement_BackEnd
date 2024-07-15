package sunjin.DeptManagement_BackEnd.domain.order.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.global.common.BaseEntity;
import sunjin.DeptManagement_BackEnd.global.enums.ApprovalStatus;
import sunjin.DeptManagement_BackEnd.global.enums.OrderType;

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
    private OrderType orderType;

    @Column(length = 100, nullable = false)
    private String storeName;

    @Column(nullable = false)
    private int totalPrice;

    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus status;

    @Column
    private String rejectionDescription;

    @Column(nullable = false)
    private String receiptImgPath;

    @Column
    private String ImgURL;

    @Column
    private LocalDateTime firstProcDate;

    @Column
    private LocalDateTime secondProcDate;


    @JoinColumn(columnDefinition = "varchar(100)",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "department_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;

    public void updateInfo(OrderType productType, String productName, int totalPrice, String description) {
        this.orderType = productType;
        this.storeName = productName;
        this.totalPrice = totalPrice;
        this.description = description;
    }

    public void submit(ApprovalStatus status, LocalDateTime firstProcDate, LocalDateTime secondProcDate) {
        this.status = status;
        this.firstProcDate = firstProcDate;
        this.secondProcDate = secondProcDate;
    }
}
