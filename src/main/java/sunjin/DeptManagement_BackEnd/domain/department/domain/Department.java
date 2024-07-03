package sunjin.DeptManagement_BackEnd.domain.department.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sunjin.DeptManagement_BackEnd.global.common.BaseEntity;
import sunjin.DeptManagement_BackEnd.global.enums.DeptType;

@SuperBuilder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "department")
public class Department extends BaseEntity {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeptType deptType;
}
