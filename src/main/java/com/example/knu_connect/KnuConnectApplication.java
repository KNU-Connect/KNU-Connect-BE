package com.example.knu_connect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class KnuConnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(KnuConnectApplication.class, args);
	}

}
