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

import java.util.Arrays;
import java.util.List;

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

        Department scmDepartment = new Department("SCM", "디지털 SCM팀");
        Department fcmDepartment = new Department("FCM", "디지털 FM팀");


        departmentRepository.save(scmDepartment);
        departmentRepository.save(fcmDepartment);

        String encodedPassword = passwordEncoder.encode("1234");

        // 멤버 초기 데이터 삽입
        List<Member> members = Arrays.asList(
                new Member("김철수", "employee1", encodedPassword, Role.EMPLOYEE, scmDepartment, null),
                new Member("이서연", "employee2", encodedPassword, Role.EMPLOYEE, scmDepartment, null),
                new Member("배경수", "teamleader1", encodedPassword, Role.TEAMLEADER, scmDepartment, null),
                new Member("최영식", "centerdirector", encodedPassword, Role.CENTERDIRECTOR, scmDepartment, null),
                new Member("윤성진", "employee3", encodedPassword, Role.EMPLOYEE, fcmDepartment, null),
                new Member("한태진", "employee4", encodedPassword, Role.EMPLOYEE, fcmDepartment, null),
                new Member("이준혁", "teamleader2", encodedPassword, Role.TEAMLEADER, fcmDepartment, null)
        );
        memberRepository.saveAll(members);

        // Order 초기 데이터 삽입
        List<Order> orders = Arrays.asList(
                new Order(OrderType.FOOD_COSTS, "수지네 식당", 35000, "부서 단체 식사", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(0), members.get(0).getDepartment()),
                new Order(OrderType.FIXTURES, "A4", 8700, "A4용지 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(0), members.get(0).getDepartment()),
                new Order(OrderType.TRANSPORTATION, "KTX", 48000, "판교 출장으로 KTX 결제", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(0), members.get(0).getDepartment()),

                new Order(OrderType.SNACK, "쿠팡", 32000, "간식 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(1), members.get(1).getDepartment()),
                new Order(OrderType.ENTERTAINMENT, "풍성옥", 52000, "판교팀 접대", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(1), members.get(1).getDepartment()),
                new Order(OrderType.ETC, "출장비", 5100, "물품 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(1), members.get(1).getDepartment()),

                new Order(OrderType.FIXTURES, "다이소", 15000, "사무실 필기구 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(2), members.get(2).getDepartment()),
                new Order(OrderType.TRANSPORTATION, "SK주유소", 45000, "이천 출장시에 차량 주유", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(2), members.get(2).getDepartment()),
                new Order(OrderType.FOOD_COSTS, "한솥도시락", 27000, "점심 도시락 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(2), members.get(2).getDepartment()),

                new Order(OrderType.FIXTURES, "이케아", 75000, "사무실 책상 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(3), members.get(3).getDepartment()),
                new Order(OrderType.SNACK, "GS25", 12000, "간식 및 음료 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(3), members.get(3).getDepartment()),
                new Order(OrderType.ENTERTAINMENT, "비비고", 56000, "고객 접대", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(3), members.get(3).getDepartment()),

                new Order(OrderType.ETC, "택시비", 25000, "고객 미팅 후 택시비", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(4), members.get(4).getDepartment()),
                new Order(OrderType.FIXTURES, "문구나라", 30000, "사무실 문구류 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(4), members.get(4).getDepartment()),
                new Order(OrderType.TRANSPORTATION, "택시", 67000, "야근", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(4), members.get(4).getDepartment()),

                new Order(OrderType.FOOD_COSTS, "김밥천국", 24000, "점심 김밥 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(5), members.get(5).getDepartment()),
                new Order(OrderType.FIXTURES, "리바트", 92000, "사무실 의자 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(5), members.get(5).getDepartment()),
                new Order(OrderType.TRANSPORTATION, "버스", 12000, "출장 교통비", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(5), members.get(5).getDepartment()),

                new Order(OrderType.SNACK, "스타벅스", 15000, "회의 간식 및 커피", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(6), members.get(6).getDepartment()),
                new Order(OrderType.ENTERTAINMENT, "비비고", 64000, "고객 점심 식사", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(6), members.get(6).getDepartment()),
                new Order(OrderType.ETC, "화원", 8000, "사무실 화분 구매", ApprovalStatus.WAIT, null, "a", "a", null, null, members.get(6), members.get(6).getDepartment())
        );
        orderRepository.saveAll(orders);

        System.out.println("초기 데이터 삽입 완료!");
    }
}
