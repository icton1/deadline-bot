package se.ifmo.pepe.icton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
public class IctonApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(IctonApplication.class, args);
    }

}