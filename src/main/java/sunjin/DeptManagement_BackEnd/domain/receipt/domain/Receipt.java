package sunjin.DeptManagement_BackEnd.domain.receipt.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.global.common.BaseEntity;

import java.time.LocalDateTime;

@SuperBuilder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "receipt")
public class Receipt extends BaseEntity {

    @Column(length = 100, nullable = false)
    private String storeName;

    @Column(nullable = false)
    private int rTotalPrice;

    @Column(nullable = false)
    private LocalDateTime recDate;

    @JoinColumn(columnDefinition = "varchar(100)",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
}
