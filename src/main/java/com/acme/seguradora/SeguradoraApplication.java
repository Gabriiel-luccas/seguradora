package com.acme.seguradora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SeguradoraApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeguradoraApplication.class, args);
    }
}
