package com.yhs.member.springboot.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

//@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@SpringBootApplication
public class MemberSpringbootJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MemberSpringbootJpaApplication.class, args);
	}

}
