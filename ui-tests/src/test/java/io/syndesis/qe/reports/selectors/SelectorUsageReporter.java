package io.syndesis.qe.reports.selectors;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.openqa.selenium.By;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import java.io.FileWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectorUsageReporter {

    private final Map<String, SelectorUsageInfo> reports;

    public SelectorUsageReporter() {
        reports = new HashMap<>();
    }

    public void report(SelectorUsageInfo info) {
        reports.put(info.selector, info);
        log.info("Registering {}", info);
    }

    public boolean wasSelectorReported(By selector) {
        return reports.containsKey(selector.toString());
    }

    public void selectorIsUsedInScenario(By selector, String scenario) {
        reports.get(selector.toString()).getScenarios().add(scenario);
    }

    /**
     * Generates report
     *
     * @param properties map of the <variables, values> to be used in the template
     * @param templatePath template source on classpath
     * @param outputPath output path of the generated template
     */
    protected void generateReport(Map<String, Object> properties, String templatePath, String outputPath) {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        engine.init();
        Template template = engine.getTemplate(templatePath);
        properties.put("utils", new ReportUtils());
        VelocityContext context = new VelocityContext(properties);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        try (FileWriter file = new FileWriter(outputPath)) {
            file.write(writer.toString());
        } catch (Exception e) {
            log.error("Something went wrong while writing the report", e);
        }
    }

    public void generateReports() {
        reportSimple();
        reportBasedOnURL();
        reportDataTestIdSelectors();
    }

    private void reportSimple() {
        Map<String, Object> reps = new HashMap<>();
        reps.put("reports", reports.values());
        generateReport(reps, "/templates/selector_report_template.vm", "target/cucumber/selector_report.html");
    }

    private void reportBasedOnURL() {
        Map<String, List<SelectorUsageInfo>> reportsByUrl = new HashMap<>();
        reports.values().stream().filter(Predicates.not(SelectorUsageInfo::hasDataTestId)).forEach(selectorUsageInfo -> {
            if (reportsByUrl.containsKey(selectorUsageInfo.url)) {
                reportsByUrl.get(selectorUsageInfo.url).add(selectorUsageInfo);
            } else {
                reportsByUrl.put(selectorUsageInfo.url, Lists.newArrayList(selectorUsageInfo));
            }
        });
        Map<String, Object> reps = new HashMap<>();
        reps.put("reports", reportsByUrl);
        generateReport(reps, "/templates/url_report_template.vm", "target/cucumber/url_report.html");
    }

    private void reportDataTestIdSelectors() {
        List<SelectorUsageInfo> info = reports.values().stream().filter(SelectorUsageInfo::hasDataTestId).collect(Collectors.toList());
        if (info.size() > 0) {
            Map<String, Object> reps = new HashMap<>();
            reps.put("reports", info);
            generateReport(reps, "/templates/data_test_id_template.vm", "target/cucumber/testid_rewrite.html");
        }
    }
}
