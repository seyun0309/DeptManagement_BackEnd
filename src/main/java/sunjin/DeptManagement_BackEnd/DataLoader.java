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
import sunjin.DeptManagement_BackEnd.domain.order.domain.Order;
import sunjin.DeptManagement_BackEnd.domain.order.repository.OrderRepository;
import sunjin.DeptManagement_BackEnd.global.enums.ApprovalStatus;
import sunjin.DeptManagement_BackEnd.global.enums.OrderType;
import sunjin.DeptManagement_BackEnd.global.enums.Role;

@Component
public class DataLoader implements ApplicationRunner {

    private final DepartmentRepository departmentRepository;
    private final MemberRepository memberRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;

    @Autowired
    public DataLoader(DepartmentRepository departmentRepository, MemberRepository memberRepository, JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, OrderRepository orderRepository) {
        this.departmentRepository = departmentRepository;
        this.memberRepository = memberRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
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


        departmentRepository.save(hrDepartment);
        departmentRepository.save(financeDepartment);

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

        Order order1 = new Order(OrderType.FOOD_COSTS, "수지네 식당", 35000, "부서 단체 식사", ApprovalStatus.WAIT, null, "a", "a", null, null, member1, member1.getDepartment());
        Order order2 = new Order(OrderType.FIXTURES, "A4", 8700, "A4용지 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, member1, member1.getDepartment());
        Order order3 = new Order(OrderType.TRANSPORTATION, "출장", 48000, "판교 출장으로 KTX 결제", ApprovalStatus.WAIT, null, "a", "a", null, null, member1, member1.getDepartment());

        Order order4 = new Order(OrderType.SNACK, "쿠팡", 32000, "간식 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, member2, member2.getDepartment());
        Order order5 = new Order(OrderType.ENTERTAINMENT, "풍성옥", 52000, "판교팀 접대", ApprovalStatus.WAIT, null, "a", "a", null, null, member2, member2.getDepartment());
        Order order6 = new Order(OrderType.ETC, "출장비", 5100, "물품 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, member2, member2.getDepartment());

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
        orderRepository.save(order4);
        orderRepository.save(order5);
        orderRepository.save(order6);

        System.out.println("초기 데이터 삽입 완료!");
    }
}
