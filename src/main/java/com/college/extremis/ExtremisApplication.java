package com.college.extremis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExtremisApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExtremisApplication.class, args);
    }
}
