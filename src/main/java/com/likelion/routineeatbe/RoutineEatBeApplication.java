package com.likelion.routineeatbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RoutineEatBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoutineEatBeApplication.class, args);
    }

}
