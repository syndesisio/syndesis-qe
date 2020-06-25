package io.syndesis.qe.hooks;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.issue.IssueState;
import io.syndesis.qe.issue.SimpleIssue;
import io.syndesis.qe.utils.IssueHooksUtils;

import org.junit.Assume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;

/**
 * Idea of these hooks: we can maintain currently open issues which causes tests to fail
 * and simultaneously history of resolved issues per scenario on one place for easier
 * fail investigation.
 */
@Slf4j
public class IssueHooks {

    /**
     * This hook skips tests that have open github and ENTESB jira issues
     * <p>
     * Only runs when {@link TestConfiguration#SKIP_TESTS_WITH_OPEN_ISSUES} property is set to true
     *
     * @param scenario
     */
    @Before
    public void skipTestsWithOpenIssues(Scenario scenario) {
        if (TestConfiguration.skipTestsWithOpenIssues()) {
            log.info(scenario.getName());
            if (scenario.getSourceTagNames().contains("@notIgnoreOpenIssue")) {
                return;
            }
            List<SimpleIssue> issues = getAllIssues(scenario);

            for (SimpleIssue issue : issues) {
                // assumeFalse will skip the test if the argument evaluates to true, i.e. when the issue is open
                Assume.assumeFalse(IssueState.OPEN.equals(issue.getState()));
            }
        }
    }

    /**
     * This hook checks and reports status of linked github and ENTESB Jira issues.
     * <p>
     * Each failed scenario is checked if it contains a tag in the form @gh-&lt;issue-number&gt; or @ENTESB-&lt;issue-number&gt;.
     * If it does, Jira or GitHub and ZenHub are queried for the status of each issue and a summary is written into log and cucumber report.
     * <p>
     * For the purposes of this report, the following issue states are recognized:
     * <ol>
     * <li>Open - an issue that was reported and not yet fixed</li>
     * <li>Done - an issue that was reported and supposedly fixed</li>
     * <li>Closed - and issue that was fixed and verified by QE</li>
     * </ol>
     * <p>
     * <p>
     * No effort is made to reason about the impact of the found issues (e.g. is it ok that a test fails if there's one open and one done issue?).
     * Also, this hook completely ignores passed tests (i.e. nothing happens when a tests with open issues passes).
     *
     * @param scenario
     */
    @After
    public void checkIssues(Scenario scenario) {
        if (!scenario.isFailed()) {
            return;
        }

        List<SimpleIssue> issues = getAllIssues(scenario);

        if (issues.isEmpty()) {
            IssueHooksUtils.logError(scenario, "############ No GitHub or Jira issue annotations found ############");
            return;
        }

        try {
            List<SimpleIssue> openIssues = new ArrayList<>();
            List<SimpleIssue> doneIssues = new ArrayList<>();
            List<SimpleIssue> closedIssues = new ArrayList<>();

            for (SimpleIssue issue : issues) {

                switch (issue.getState()) {
                    case DONE:
                        doneIssues.add(issue);
                        break;
                    case OPEN:
                        openIssues.add(issue);
                        break;
                    case CLOSED:
                        closedIssues.add(issue);
                        break;
                }
            }

            IssueHooksUtils.logError(scenario, "############ FAILED PROBABLY DUE TO: ################");
            IssueHooksUtils.logError(scenario, "######## DONE issues ########");
            IssueHooks.logIssues(scenario, doneIssues);
            IssueHooksUtils.logError(scenario, "######## CLOSED issues ########");
            IssueHooks.logIssues(scenario, closedIssues);
            IssueHooksUtils.logError(scenario, "######## OPEN issues ########");
            IssueHooks.logIssues(scenario, openIssues);
            embedIssues(scenario, issues);
        } catch (Exception e) {
            log.error("Error while processing GH & Jira issues", e);
            scenario.attach("Error while processing GH & Jira issues".getBytes(), "text/plain", "ErrorMessage");
            e.printStackTrace();
        }
    }

    private void embedIssues(Scenario scenario, List<SimpleIssue> issues) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode array = mapper.createArrayNode();

        for (SimpleIssue issue : issues) {
            array.addPOJO(issue);
        }

        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, array);
        scenario.attach(sw.toString().getBytes(), "application/x.issues+json", "Issues");
    }

    public static List<SimpleIssue> getAllIssues(Scenario scenario) {
        List<String> jiraIssues = scenario.getSourceTagNames().stream().filter(t -> t.matches("^@ENTESB-\\d+$")).collect(Collectors.toList());

        if (jiraIssues.isEmpty()) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(IssueHooksUtils.analyzeJiraIssues(jiraIssues, scenario));
        }
    }

    private static void logIssues(Scenario scenario, List<SimpleIssue> issues) {
        for (SimpleIssue issue : issues) {
            IssueHooksUtils.logError(scenario, "#### Title: " + issue.getIssueSummary());
            IssueHooksUtils.logError(scenario, "#### Link: " + issue.getUrl());
            IssueHooksUtils.logError(scenario, "----------------------------------------");
        }
    }
}
