package edu.demart_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DemartApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemartApiApplication.class, args);
		
}

}
