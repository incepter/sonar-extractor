package dev.incepter.sonarextractor.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class Extractor {
    private static final RestTemplate TEMPLATE = new RestTemplate();
    private static final List<String> ISSUES_HEADERS_LIST = Arrays.asList(
            "key",
            "rule",
            "severity",
            "component",
            "project",
            "status",
            "message",
            "effort",
            "debt",
            "author",
            "type",
            "scope",
            "quickFixAvailable"
    );
    private static final List<String> HOTSPOTS_HEADERS_LIST = Arrays.asList(
            "key",
            "component",
            "project",
            "securityCategory",
            "vulnerabilityProbability",
            "status",
            "line",
            "message",
            "author",
            "creationDate",
            "updateDate"
    );
    private final ExtractorProperties properties;


    private SonarIssueResponse getIssues(final String project, int page, int size) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(properties.getAuthorization());

            final HttpEntity<?> entity = new HttpEntity<>(null, headers);

            final String url = properties.makeIssuesUrl("componentKeys=" + project + "&ps=" + size + "&p=" + page);
            log.info("getting issues using url {}", url);
            return TEMPLATE.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    SonarIssueResponse.class
            ).getBody();
        } catch (RestClientException e) {
            log.error("Error occurred while getting issues:", e);
            throw new RuntimeException(e);
        }
    }


    public List<SonarIssueResponse.Issue> fetchIssues(final String project) {
        int currentPage = 1; // starts with 1
        final List<SonarIssueResponse.Issue> allIssues = new ArrayList<>();
        do {
            final SonarIssueResponse response = getIssues(project, currentPage, 500);
            log.info("retrieved issues response from sonar about project {} with current page {}: page={} size={} total elements = {}",
                    project, currentPage, response.getPage(), response.getPageSize(), response.getTotal());
            final List<SonarIssueResponse.Issue> issues = response.getIssues();
            if (Objects.isNull(issues) || issues.isEmpty()) {
                log.info("Encountered 0 issues response for project {} in page {}. Ending extraction", project, currentPage);
                break;
            }
            log.info("Adding {} issues response to list for project {} in page {}", issues.size(), project, currentPage);
            allIssues.addAll(issues);
            currentPage += 1;
        } while (allIssues.size() + 500 <= 10000);

        return allIssues;
    }

    private SonarHotspotResponse getHotspots(final String project, int page, int size) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(properties.getAuthorization());

            final HttpEntity<?> entity = new HttpEntity<>(null, headers);

            final String url = properties.makeHotspotsUrl("projectKey=" + project + "&ps=" + size + "&p=" + page);
            log.info("getting hotspots using url {}", url);
            return TEMPLATE.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    SonarHotspotResponse.class
            ).getBody();
        } catch (RestClientException e) {
            log.error("Error occurred:", e);
            throw new RuntimeException(e);
        }
    }

    public List<SonarHotspotResponse.Hotspot> fetchHotspots(final String project) {
        int currentPage = 1; // starts with 1
        final List<SonarHotspotResponse.Hotspot> allHotspots = new ArrayList<>();
        do {
            final SonarHotspotResponse response = getHotspots(project, currentPage, 500);
            final SonarHotspotResponse.Paging paging = response.getPaging();
            log.info("retrieved hotspots response from sonar about project {} with current page {}: page={} size={} total elements = {}",
                    project, currentPage, paging.getPageIndex(), paging.getPageSize(), paging.getTotal());
            final List<SonarHotspotResponse.Hotspot> hotspots = response.getHotspots();
            if (Objects.isNull(hotspots) || hotspots.isEmpty()) {
                log.info("Encountered 0 hotspots response for project {} in page {}. Ending extraction", project, currentPage);
                break;
            }
            log.info("Adding {} hotspots response to list for project {} in page {}", hotspots.size(), project, currentPage);
            allHotspots.addAll(hotspots);
            currentPage += 1;
        } while (allHotspots.size() + 500 <= 10000);

        return allHotspots;
    }

    public void extract(final String project) {
        final boolean isExtractIssuesEnabled = properties.isExtractIssuesEnabled();
        final boolean isExtractHotspotsEnabled = properties.isExtractHotspotsEnabled();
        if (!isExtractIssuesEnabled && !isExtractHotspotsEnabled) {
            log.info("extraction is disabled for both issues and hotspots.");
            return;
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            if (isExtractIssuesEnabled) {
                final List<SonarIssueResponse.Issue> allIssues = fetchIssues(project);
                final AtomicInteger rowIndex = new AtomicInteger(-1);
                final XSSFSheet sheet = workbook.createSheet(project + " issues");

                log.info("saving {} issues for project {}", allIssues.size(), project);
                addRow(sheet, rowIndex.incrementAndGet(), ISSUES_HEADERS_LIST);

                allIssues.forEach(issue -> {
                    addRow(sheet, rowIndex.incrementAndGet(), Arrays.asList(
                            issue.getKey(),
                            issue.getRule(),
                            issue.getSeverity(),
                            issue.getComponent(),
                            issue.getProject(),
                            issue.getStatus(),
                            issue.getMessage(),
                            issue.getEffort(),
                            issue.getDebt(),
                            issue.getAuthor(),
                            issue.getType(),
                            issue.getScope(),
                            issue.getQuickFixAvailable()
                    ));
                });
            }
            if (isExtractHotspotsEnabled) {
                final List<SonarHotspotResponse.Hotspot> allHotspots = fetchHotspots(project);
                final AtomicInteger rowIndex = new AtomicInteger(-1);
                final XSSFSheet sheet = workbook.createSheet(project + " hotspots");

                log.info("saving {} hotspots for project {}", allHotspots.size(), project);
                addRow(sheet, rowIndex.incrementAndGet(), HOTSPOTS_HEADERS_LIST);

                allHotspots.forEach(hotspot -> {
                    addRow(sheet, rowIndex.incrementAndGet(), Arrays.asList(
                            hotspot.getKey(),
                            hotspot.getComponent(),
                            hotspot.getProject(),
                            hotspot.getSecurityCategory(),
                            hotspot.getVulnerabilityProbability(),
                            hotspot.getStatus(),
                            hotspot.getLine(),
                            hotspot.getMessage(),
                            hotspot.getAuthor(),
                            hotspot.getCreationDate(),
                            hotspot.getUpdateDate()
                    ));
                });
            }


            final File extractionDir = new File(properties.getExtractionDirectory());
            if (!extractionDir.exists()) {
                extractionDir.mkdirs();
            }
            final String filename = properties.getExtractionDirectory().concat(project).concat("-analysis.xlsx");
            final FileOutputStream out = new FileOutputStream(new File(filename));
            workbook.write(out);
            out.close();
            log.info("{} written successfully on disk.", filename);
        } catch (IOException e) {
            log.error("Error occurred:", e);
            throw new RuntimeException(e);
        }
    }

    private void addRow(final XSSFSheet sheet, int rowIndex, List<String> content) {
        final XSSFRow row = sheet.createRow(rowIndex);
        for (int i = 0; i < content.size(); i++) {
            final XSSFCell cell = row.createCell(i);
            cell.setCellValue(content.get(i));
        }
    }
}
