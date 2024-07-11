package sunjin.DeptManagement_BackEnd.domain.receipt.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
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

    @Column(nullable = false)
    private String receiptImgPath;

    @Column(length = 100, nullable = false)
    private String storeName;

    @Column(nullable = false)
    private int receiptPrice;

    @Column(nullable = false)
    private LocalDateTime recDate;

    @Column
    private String ImgURL;

    @JoinColumn(columnDefinition = "varchar(100)",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "department_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;
}
