package sunjin.DeptManagement_BackEnd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.department.repository.DepartmentRepository;
import sunjin.DeptManagement_BackEnd.domain.member.domain.Member;
import sunjin.DeptManagement_BackEnd.domain.member.repository.MemberRepository;
import sunjin.DeptManagement_BackEnd.global.enums.Role;

@Component
public class DataLoader implements ApplicationRunner {

    private final DepartmentRepository departmentRepository;
    private final MemberRepository memberRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataLoader(DepartmentRepository departmentRepository, MemberRepository memberRepository, JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.memberRepository = memberRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 부서 초기 데이터 삽입
        memberRepository.deleteAll();
        departmentRepository.deleteAll();

        // 시퀀스 초기화
        jdbcTemplate.execute("ALTER SEQUENCE department_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE member_id_seq RESTART WITH 1");

        Department hrDepartment = new Department("HR", "Human Resources");
        Department financeDepartment = new Department("FIN", "Finance");
        Department itDepartment = new Department("IT", "IT");
        Department adminDepartment = new Department("Admin", "Admin");

        departmentRepository.save(hrDepartment);
        departmentRepository.save(financeDepartment);
        departmentRepository.save(itDepartment);
        departmentRepository.save(adminDepartment);

        // 관리자 초기 데이터 삽입
        String encodedPassword = passwordEncoder.encode("1234");
        Member member = new Member("seyun", "admin", encodedPassword, Role.ADMIN, adminDepartment, null);
        memberRepository.save(member);


        System.out.println("초기 데이터 삽입 완료!");
    }
}
