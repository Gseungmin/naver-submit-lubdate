package com.example.naver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NaverApplication {

	public static void main(String[] args) {
		SpringApplication.run(NaverApplication.class, args);
	}
}
