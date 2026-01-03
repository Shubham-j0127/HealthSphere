package com.healthsphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HealthSphereApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthSphereApplication.class, args);
    }
}

