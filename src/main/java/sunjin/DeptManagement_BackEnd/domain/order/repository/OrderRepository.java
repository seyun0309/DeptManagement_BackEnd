package sunjin.DeptManagement_BackEnd.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.department.id = :departmentId")
    List<Order> findAllByDeletedAtIsNull(@Param("departmentId") Long departmentId);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL")
    List<Order> findAllByDeletedIsNull();
}
