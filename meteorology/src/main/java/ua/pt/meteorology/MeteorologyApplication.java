package ua.pt.meteorology;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author dn
 */
@SpringBootApplication
public class MeteorologyApplication {
    
    /**
     * Project main function.
     * @param args 
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MeteorologyApplication.class);
        app.run(args);
    }
}
