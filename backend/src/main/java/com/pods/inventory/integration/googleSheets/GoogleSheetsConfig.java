package com.pods.inventory.integration.googleSheets;

import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.pods.inventory.config.SheetsProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleSheetsConfig {

    @Bean
    public Sheets sheets(SheetsProperties properties) throws Exception {
        if (properties.credentialsFile() == null || properties.credentialsFile().isBlank()) {
            throw new IllegalStateException("Defina GOOGLE_APPLICATION_CREDENTIALS com o arquivo JSON da service account.");
        }

        GoogleCredentials credentials;
        try (var inputStream = Files.newInputStream(Path.of(properties.credentialsFile()))) {
            credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(List.of(SheetsScopes.SPREADSHEETS));
        }

        return new Sheets.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(properties.applicationName()).build();
    }
}
