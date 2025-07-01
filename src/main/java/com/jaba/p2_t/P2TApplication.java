package com.jaba.p2_t;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.jaba.p2_t.bootinit.PostgresBootstrapper;




@SpringBootApplication

public class P2TApplication {

	public static void main(String[] args) {
		PostgresBootstrapper.init();
		SpringApplication.run(P2TApplication.class, args);

	}

}
