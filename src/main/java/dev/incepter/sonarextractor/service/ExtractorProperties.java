package dev.incepter.sonarextractor.service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ToString
@Component
@Validated
@ConfigurationProperties(prefix = "extractor.sonar")
public class ExtractorProperties {

    @NonNull
    private String baseUrl;
    @NonNull
    private String issuesSearchUrl;
    @NonNull
    private String hotspotsSearchUrl;
    @NonNull
    private String authorization;
    @NonNull
    private Boolean extractIssues;
    @NonNull
    private Boolean extractHotspots;
    @NonNull
    private String extractionDirectory;

    public boolean isExtractIssuesEnabled() {
        return Boolean.TRUE.equals(extractIssues);
    }

    public boolean isExtractHotspotsEnabled() {
        return Boolean.TRUE.equals(extractHotspots);
    }

    public String makeIssuesUrl(final String search) {
        return baseUrl.concat(issuesSearchUrl).concat("?").concat(search);
    }
    public String makeHotspotsUrl(final String search) {
        return baseUrl.concat(hotspotsSearchUrl).concat("?").concat(search);
    }
}
