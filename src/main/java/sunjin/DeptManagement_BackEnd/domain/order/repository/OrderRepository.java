package sunjin.DeptManagement_BackEnd.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
import sunjin.DeptManagement_BackEnd.global.enums.ApprovalStatus;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.member.id = :memberId")
    List<Order> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.member.id = :memberId AND o.status = :status")
    List<Order> findAllByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") ApprovalStatus status);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.member.id = :memberId AND o.status IN :statuses")
    List<Order> findByMemberIdAndStatusIn(@Param("memberId") Long memberId, @Param("statuses") List<ApprovalStatus> statuses);
}
