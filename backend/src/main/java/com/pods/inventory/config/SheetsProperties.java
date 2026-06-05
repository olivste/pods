package com.pods.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sheets")
public record SheetsProperties(
        String applicationName,
        String spreadsheetId,
        String credentialsFile,
        String productsSheet,
        String salesSheet,
        String movementsSheet
) {
}
