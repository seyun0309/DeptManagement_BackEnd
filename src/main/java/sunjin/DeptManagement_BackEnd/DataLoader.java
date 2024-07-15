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

        // 멤버 초기 데이터 삽입
        String encodedPassword1 = passwordEncoder.encode("1234");
        Member member1 = new Member("Kim", "employee1", encodedPassword1, Role.EMPLOYEE, hrDepartment, null);

        String encodedPassword2 = passwordEncoder.encode("1234");
        Member member2 = new Member("Hong", "employee2", encodedPassword2, Role.EMPLOYEE, hrDepartment, null);

        String encodedPassword3 = passwordEncoder.encode("1234");
        Member member3 = new Member("Hwang", "teamleader", encodedPassword3, Role.TEAMLEADER, hrDepartment, null);

        String encodedPassword4 = passwordEncoder.encode("1234");
        Member member4 = new Member("CHOI", "centerdirector", encodedPassword4, Role.CENTERDIRECTOR, hrDepartment, null);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);


        System.out.println("초기 데이터 삽입 완료!");
    }
}
