package sunjin.DeptManagement_BackEnd.domain.notification.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.global.common.BaseEntity;

@SuperBuilder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notification")
public class Notification extends BaseEntity {

    @Column
    private String message;

    @Column
    private Boolean isRead;

    @JoinColumn(columnDefinition = "varchar(100)", nullable = false)
    @ManyToOne
    private Member receiver;

    public void read() {
        this.isRead = true;
    }
}
