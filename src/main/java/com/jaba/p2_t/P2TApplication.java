package com.jaba.p2_t;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.jaba.p2_t.bootinit.MainInit;


@SpringBootApplication
@EnableScheduling
public class P2TApplication {

	public static void main(String[] args) {
		MainInit.mainInit();
		SpringApplication.run(P2TApplication.class, args);

	}

}
