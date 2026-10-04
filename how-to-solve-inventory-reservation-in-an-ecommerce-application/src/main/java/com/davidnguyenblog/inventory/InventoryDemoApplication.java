package com.davidnguyenblog.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InventoryDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryDemoApplication.class, args);
    }
}
