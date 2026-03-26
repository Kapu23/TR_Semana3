package com.edgar.pacientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.edgar.pacientes", "com.edgar.commons"})
public class PacientesMsvApplication {

	public static void main(String[] args) {
		SpringApplication.run(PacientesMsvApplication.class, args);
	}

}
