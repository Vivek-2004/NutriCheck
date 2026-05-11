package com.nutricheck;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NutriCheckApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().load();
		for (DotenvEntry i : dotenv.entries()) {
			String key = i.getKey();
			String value = i.getValue();
			System.setProperty(key, value);
		}

		SpringApplication.run(NutriCheckApplication.class, args);
	}
}