package dev.incepter.sonarextractor.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SonarIssueResponse {
    int total;
    @JsonProperty("p")
    int page;
    @JsonProperty("ps")
    int pageSize;
    Paging paging;

    List<Issue> issues;


    @Data
    public static class Paging {
        int pageIndex;
        int pageSize;
        int total;
    }


    @Data
    public static class Issue {
        String key;
        String rule;
        String severity;
        String component;
        String project;
        String status;
        String message;
        String effort;
        String debt;
        String author;
        String type;
        String scope;
        String quickFixAvailable;
    }



}
