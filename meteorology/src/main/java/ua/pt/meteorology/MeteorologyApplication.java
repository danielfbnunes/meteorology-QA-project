package ua.pt.meteorology;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MeteorologyApplication {

	public static void main(String[] args) {
            SpringApplication app = new SpringApplication(MeteorologyApplication.class);
            app.run(args);
	}

}
