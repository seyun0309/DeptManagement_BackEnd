package sunjin.DeptManagement_BackEnd.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
import sunjin.DeptManagement_BackEnd.global.enums.ApprovalStatus;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.member.id = :memberId AND o.status IN :status")
    List<Order> findAllByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") List<ApprovalStatus> status);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.member.id = :memberId AND o.status IN :status")
    List<Order> findByMemberIdAndStatusIn(@Param("memberId") Long memberId, @Param("status") List<ApprovalStatus> status);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.status IN :status")
    List<Order> findByStatus(@Param("status") List<ApprovalStatus> status);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.status = :status")
    List<Order> findByStatusIsProgress(@Param("status") ApprovalStatus status);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.department.id = :departmentId AND o.status IN :status")
    List<Order> findByDepartmentIdAndStatusIn(@Param("departmentId") Long departmentId, @Param("status") List<ApprovalStatus> status);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.department.id = :departmentId AND o.status IN :status")
    List<Order> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, @Param("status") List<ApprovalStatus> status);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.department.id = :departmentId AND o.member.id = :memberId AND o.status IN :status")
    List<Order> findByDepartmentIdAndMemberAndStatusIn(@Param("departmentId") Long departmentId, @Param("memberId") Long memberId, @Param("status") List<ApprovalStatus> status);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.department.id = :departmentId AND o.member.id = :memberId AND o.status IN :status")
    List<Order> findByDepartmentIdAndMemberAndStatus(@Param("departmentId") Long departmentId, @Param("memberId") Long memberId, @Param("status") List<ApprovalStatus> status);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.member.id = :memberId AND o.status IN :status")
    List<Order> findAllByMemberIdAndStatusIn(@Param("memberId") Long memberId, @Param("status") List<ApprovalStatus> status);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.status IN :status")
    List<Order> findByStatusIn(@Param("status") List<ApprovalStatus> status);
}
