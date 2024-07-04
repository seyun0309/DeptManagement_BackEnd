package sunjin.DeptManagement_BackEnd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import sunjin.DeptManagement_BackEnd.domain.department.domain.Department;
import sunjin.DeptManagement_BackEnd.domain.department.repository.DepartmentRepository;

@Component
public class DataLoader implements ApplicationRunner {

    private final DepartmentRepository departmentRepository;

    @Autowired
    public DataLoader(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 데이터베이스에 초기 데이터 삽입
        Department hrDepartment = new Department("HR", "Human Resources");
        Department financeDepartment = new Department("FIN", "Finance");
        Department itDepartment = new Department("IT", "IT");

        departmentRepository.save(hrDepartment);
        departmentRepository.save(financeDepartment);
        departmentRepository.save(itDepartment);

        System.out.println("초기 데이터 삽입 완료!");
    }
}
