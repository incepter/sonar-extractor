package dev.incepter.sonarextractor.service;

import lombok.Data;

import java.util.List;

@Data
public class SonarHotspotResponse {
    Paging paging;

    List<Hotspot> hotspots;

    @Data
    public static class Paging {
        int pageIndex;
        int pageSize;
        int total;
    }


    @Data
    public static class Hotspot {
        String key;
        String component;
        String project;
        String securityCategory;
        String vulnerabilityProbability;
        String status;
        String line;
        String message;
        String author;
        String creationDate;
        String updateDate;
    }



}
