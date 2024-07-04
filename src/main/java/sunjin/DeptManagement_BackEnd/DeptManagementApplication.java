package sunjin.DeptManagement_BackEnd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DeptManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeptManagementApplication.class, args);
	}

}
