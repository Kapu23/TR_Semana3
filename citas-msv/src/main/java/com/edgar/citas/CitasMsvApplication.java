package com.edgar.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.edgar.citas", "com.edgar.commons"})
public class CitasMsvApplication {

	public static void main(String[] args) {
		SpringApplication.run(CitasMsvApplication.class, args);
	}

}
