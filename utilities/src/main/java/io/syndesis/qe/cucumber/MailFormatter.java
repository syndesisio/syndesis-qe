package io.syndesis.qe.cucumber;

import io.syndesis.qe.issue.IssueState;
import io.syndesis.qe.issue.SimpleIssue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.event.EmbedEvent;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestSourceRead;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.ast.Comment;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MailFormatter implements EventListener {

    private static final Pattern SUSTAINER_PATTERN = Pattern.compile(".*@sustainer: ([a-zA-Z@.]+)");

    private final String path;

    private ObjectMapper mapper = new ObjectMapper();

    private Map<String, String> sustainers = new HashMap<>();
    private Map<String, Feature> features = new HashMap<>();
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
        if ("application/x.issues+json".equals(t.mimeType)) {
            try {
                List<SimpleIssue> issues = mapper.readValue(t.data, new TypeReference<List<SimpleIssue>>() {
                });
                scenarioIssues.put(t.getTestCase().getScenarioDesignation(), issues);
            } catch (IOException e) {
                // ignoring is ok - there simply won't be an issue list in the report
                log.error("Unable to process embedded json issues", e);
            }
        }
    }

    private void onTestSourceRead(TestSourceRead t) {
        GherkinDocument doc = parseGherkinSource(t.source);
        if (doc != null) {
            features.put(t.uri, doc.getFeature());
            for (Comment c : doc.getComments()) {
                Matcher matcher = SUSTAINER_PATTERN.matcher(c.getText());
                if (matcher.matches()) {
                    sustainers.put(t.uri, matcher.group(1));
                }
            }
        }
    }

    private void onTestCaseFinished(TestCaseFinished t) {
        String uri = t.getTestCase().getUri();
        results.add(
            new ScenarioResult(
                features.get(uri), t.getTestCase(), t.result.getStatus(),
                sustainers.getOrDefault(uri, "NO SUSTAINER"), scenarioIssues.get(t.getTestCase().getScenarioDesignation()))
        );
        if (!t.result.getStatus().equals(Result.Type.PASSED) && sustainers.get(uri) != null) {
            recipients.add(sustainers.get(uri));
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
                out.write(String.format("<li>%s - %s</li>\n", issue.toLink(), issue.getIssueSummary()));
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

    private GherkinDocument parseGherkinSource(String source) {
        Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
        try {
            return parser.parse(source);
        } catch (ParserException e) {
            log.error("Error parsing gherkin source", e);
        }
        return null;
    }

    @Data
    @RequiredArgsConstructor
    private static class ScenarioResult {
        private final Feature feature;
        private final TestCase testCase;
        private final Result.Type result;
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

            if (result.equals(Result.Type.PASSED)) {
                sb
                    .append("<font color=\"green\">")
                    .append(result)
                    .append("</font>");
            } else if (result.equals(Result.Type.SKIPPED)) {
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
