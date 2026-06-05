package com.pods.inventory;

import com.pods.inventory.config.SheetsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PodsInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(PodsInventoryApplication.class, args);
    }
}
