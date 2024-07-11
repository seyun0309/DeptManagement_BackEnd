package sunjin.DeptManagement_BackEnd.domain.receipt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sunjin.DeptManagement_BackEnd.domain.receipt.domain.Receipt;

import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findAllById(Long departmentId);
}
