package dev.incepter.sonarextractor;

import dev.incepter.sonarextractor.service.Extractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
@EnableConfigurationProperties
public class SonarExtractorApplication implements CommandLineRunner {
	private final Extractor extractor;

	public static void main(String[] args) {
		SpringApplication.run(SonarExtractorApplication.class, args);
	}

	@Override
	public void run(String... args) {
		extractor.extract("allexx-mobile");
		extractor.extract("allexx-api");
		extractor.extract("allexx-location-service");
		extractor.extract("allexx-notification-service");
		extractor.extract("allexx-admin-frontend");
		extractor.extract("allexx-dynamic-website-frontend");
		extractor.extract("allexx-dynamic-website-backend");
		log.info("Finished extracting!");
	}
}
