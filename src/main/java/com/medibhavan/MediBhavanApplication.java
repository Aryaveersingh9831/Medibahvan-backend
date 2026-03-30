package com.medibhavan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class MediBhavanApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediBhavanApplication.class, args);
        System.out.println("\n🚀 MediBhavan API running on http://localhost:5000");
        System.out.println("📋 Health check: http://localhost:5000/api/health\n");
    }
}
