package com.yhs.board.springboot.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class BoardSpringbootJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoardSpringbootJpaApplication.class, args);
	}

}
