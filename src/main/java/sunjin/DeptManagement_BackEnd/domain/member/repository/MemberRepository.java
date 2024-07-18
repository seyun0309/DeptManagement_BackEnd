package sunjin.DeptManagement_BackEnd.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.global.enums.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);

    @Modifying
    @Query("UPDATE Member m SET m.refreshToken = :refreshToken WHERE m.id = :id")
    void updateRefreshToken(@Param("id") Long id, @Param("refreshToken") String refreshToken);

    @Query("SELECT m FROM Member m WHERE m.department.id = :departmentId")
    List<Member> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Query("SELECT m FROM Member m WHERE m.department.id = :departmentId AND m.role IN :role")
    List<Member> findByDepartmentId(@Param("departmentId") Long departmentId, @Param("role") List<Role> role);
}
