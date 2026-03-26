package fr.dauphine.miage.finance_tracker_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinanceTrackerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceTrackerApiApplication.class, args);
	}

}
