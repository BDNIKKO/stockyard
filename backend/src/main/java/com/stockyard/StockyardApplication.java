package com.stockyard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class StockyardApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockyardApplication.class, args);
    }
}
