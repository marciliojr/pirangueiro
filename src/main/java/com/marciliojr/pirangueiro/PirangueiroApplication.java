package com.marciliojr.pirangueiro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PirangueiroApplication {

	public static void main(String[] args) {
		SpringApplication.run(PirangueiroApplication.class, args);
	}

}
