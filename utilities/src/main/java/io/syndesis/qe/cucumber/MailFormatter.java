package io.syndesis.qe.cucumber;

import io.syndesis.qe.issue.IssueState;
import io.syndesis.qe.issue.SimpleIssue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.cucumber.core.gherkin.messages.internal.gherkin.GherkinDocumentBuilder;
import io.cucumber.core.gherkin.messages.internal.gherkin.Parser;
import io.cucumber.messages.IdGenerator;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestSourceRead;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MailFormatter implements EventListener {

    private final FeaturesInformationStore featuresInformationStore = FeaturesInformationStore.getInstance();

    private static final Pattern SUSTAINER_PATTERN = Pattern.compile(".*@sustainer: ([a-zA-Z@.]+)");

    private final String path;

    private ObjectMapper mapper = new ObjectMapper();
    private Map<String, List<SimpleIssue>> scenarioIssues = new HashMap<>();
    private Set<String> recipients = new HashSet<>();
    private List<ScenarioResult> results = new ArrayList<>();

    public MailFormatter(String path) {
        this.path = path;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::onTestSourceRead);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::onTestRunFinished);
        publisher.registerHandlerFor(EmbedEvent.class, this::onEmbed);
    }

    private void onEmbed(EmbedEvent t) {
        if ("application/x.issues+json".equals(t.getMediaType())) {
            try {
                List<SimpleIssue> issues = mapper.readValue(t.getData(), new TypeReference<List<SimpleIssue>>() {
                });
                scenarioIssues.put(t.getTestCase().getScenarioDesignation(), issues);
            } catch (IOException e) {
                // ignoring is ok - there simply won't be an issue list in the report
                log.error("Unable to process embedded json issues", e);
            }
        }
    }

    private void onTestSourceRead(TestSourceRead t) { // called before running tests to read the features
        Messages.GherkinDocument doc = parseGherkinSource(t.getSource());
        featuresInformationStore.getFeatures().put(t.getUri(), doc.getFeature()); //t.getUri() == classpath:features/check-metering-labels.feature
        for (Messages.GherkinDocument.Comment c : doc.getCommentsList()) {
            Matcher matcher = SUSTAINER_PATTERN.matcher(c.getText());
            if (matcher.matches()) {
                featuresInformationStore.getSustainers().put(t.getUri(), matcher.group(1));
            }
        }
    }

    private void onTestCaseFinished(TestCaseFinished t) {
        URI uri = t.getTestCase().getUri();
        results.add(
            new ScenarioResult(
                featuresInformationStore.getFeatures().get(uri), t.getTestCase(), t.getResult().getStatus(),
                featuresInformationStore.getSustainers().getOrDefault(uri, "NO SUSTAINER"),
                scenarioIssues.get(t.getTestCase().getScenarioDesignation()))
            // scenario designation: "features/check-prod-versions.feature:12 # Check artifacts in integration"
        );

        if (!t.getResult().getStatus().equals(Status.PASSED) && featuresInformationStore.getSustainers().get(uri) != null) {
            recipients.add(featuresInformationStore.getSustainers().get(uri));
        }
    }

    private void reportOpenIssues() {
        List<SimpleIssue> allOpenIssues = new ArrayList<>();
        results.forEach(scenarioResult -> {
            if (scenarioResult.getIssues() != null) {
                scenarioResult.getIssues().forEach(issue -> {
                    if (IssueState.OPEN == issue.getState()) {
                        allOpenIssues.add(issue);
                    }
                });
            }
        });
        if (allOpenIssues.isEmpty()) {
            return;
        }

        try (FileWriter out = new FileWriter(new File(path, "issues.html"))) {

            for (SimpleIssue issue : allOpenIssues) {
                out.write(String.format("%s - %s\n", issue.toLink(), issue.getIssueSummary()));
            }
        } catch (IOException e) {
            log.error("Error writing issues report file", e);
        }
    }

    private void onTestRunFinished(TestRunFinished t) {
        new File(path).mkdirs();
        try (FileWriter out = new FileWriter(new File(path, "report.html"))) {
            for (String line : results.stream().map(ScenarioResult::toString).collect(Collectors.toList())) {
                out.write(line + " <br/>\n");
            }
            reportOpenIssues();
        } catch (IOException e) {
            log.error("Error writing mail report file", e);
        }
        try (FileWriter out = new FileWriter(new File(path, "mail-recipients"))) {
            out.write(String.join("\n", recipients) + "\n");
        } catch (IOException e) {
            log.error("Error writing mail recipients file", e);
        }
    }

    private Messages.GherkinDocument parseGherkinSource(String source) {
        return new Parser<>(new GherkinDocumentBuilder(new IdGenerator.Incrementing())).parse(source).build();
    }

    @Data
    @RequiredArgsConstructor
    private static class ScenarioResult {
        private final Messages.GherkinDocument.Feature feature;
        private final TestCase testCase;
        private final Status result;
        private final String sustainer;
        private final List<SimpleIssue> issues;

        @Override
        public String toString() {
            // TODO: consider using a templating engine for this
            StringBuilder sb = new StringBuilder();
            sb
                .append(feature.getName())
                .append(" | ")
                .append(testCase.getName())
                .append(" | ");

            if (result.equals(Status.PASSED)) {
                sb
                    .append("<font color=\"green\">")
                    .append(result)
                    .append("</font>");
            } else if (result.equals(Status.SKIPPED)) {
                sb
                    .append("<font color=\"yellow\">")
                    .append(result)
                    .append("</font>");
            } else {
                sb
                    .append("<font color=\"red\"><b>")
                    .append(result)
                    .append("</b></font>");
            }

            sb
                .append(" | ")
                .append(sustainer);

            if (issues != null) {
                for (SimpleIssue issue : issues) {
                    if (issue.getState() == IssueState.OPEN) {
                        sb.append(" | ");
                        sb.append(issue.toLink());
                        sb.append("&nbsp;");
                    }
                }
            }

            return sb.toString();
        }
    }
}
