package io.syndesis.qe.hooks;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.issue.IssueState;
import io.syndesis.qe.issue.SimpleIssue;

import org.junit.Assume;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JiraIssueHooks {

    @Before
    public void skipTestsWithOpenJiraIssues(Scenario scenario) {
        if (TestConfiguration.skipTestsWithOpenIssues()) {

            List<Issue> issues = getJiraIssues(scenario);

            for (Issue issue : issues) {
                Assume.assumeFalse(IssueState.OPEN.equals(getIssueState(issue)));
            }
        }
    }

    /**
     * This hook checks and reports status of linked jira issues.
     * <p>
     * Each failed scenario is checked if it contains a tag in the form @ENTESB-&lt;issue-number&gt;.
     * If it does, jira is queried for the status of each issue and a summary is written into log and cucumber report.
     * <p>
     * For the purposes of this report, the following issue states are recognized:
     * <ol>
     * <li>Open - an issue that was reported and not yet fixed, the issue may be in the states: Triage backlog", "Release backlog", "In development", etc.</li>
     * <li>Done - an issue that was reported and supposedly fixed</li>
     * <li>Closed - and issue that was fixed and verified by QE</li>
     * </ol>
     * <p>
     * <p>
     *
     * @param scenario
     */
    @After
    public void checkJiraIssues(Scenario scenario) {
        if (!scenario.isFailed()) {
            return;
        }

        List<Issue> issues = getJiraIssues(scenario);

        if (issues.isEmpty()) {
            logError(scenario, "############ No Jira issue annotations found ############");
            return;
        }

        try {
            List<Issue> openJiraIssues = new ArrayList<>();
            List<Issue> doneJiraIssues = new ArrayList<>();
            List<Issue> closedJiraIssues = new ArrayList<>();

            for (Issue issue : issues) {

                switch (getIssueState(issue)) {
                    case DONE:
                        doneJiraIssues.add(issue);
                        break;
                    case OPEN:
                        openJiraIssues.add(issue);
                        break;
                    case CLOSED:
                        closedJiraIssues.add(issue);
                        break;
                }
            }

            logError(scenario, "############ FAILED PROBABLY DUE TO: ################");
            logError(scenario, "######## DONE Jira issues ########");
            logIssues(scenario, doneJiraIssues);
            logError(scenario, "######## CLOSED Jira issues ########");
            logIssues(scenario, closedJiraIssues);
            logError(scenario, "######## OPEN Jira issues ########");
            logIssues(scenario, openJiraIssues);
            embedJiraIssues(scenario, issues);
        } catch (Exception e) {
            log.error("Error while processing Jira issues", e);
            scenario.embed("Error while processing Jira issues".getBytes(), "text/plain");
            e.printStackTrace();
        }
    }

    private void embedJiraIssues(Scenario scenario, List<Issue> issues) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode array = mapper.createArrayNode();

        for (Issue issue : issues) {
            array.addPOJO(new SimpleIssue(issue.getKey(), issue.getSelf().toString(), getIssueState(issue)));
        }

        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, array);
        scenario.embed(sw.toString().getBytes(), "application/x.issues+json");
    }

    private static void logIssues(Scenario scenario, List<Issue> issues) {
        for (Issue issue : issues) {
            logError(scenario, "#### Title: " + issue.getSummary());
            logError(scenario, "#### Link: " + issue.getSelf());
            logError(scenario, "----------------------------------------");
        }
    }

    /**
     * The issues in ENTESB contain several states that are covered by the umbrella "Open" state - "Triage backlog", "Release backlog", "In
     * development", ...
     *
     * @param issue
     * @return
     */
    public IssueState getIssueState(Issue issue) {
        if ("closed".equals(issue.getStatus().getName())) {
            return IssueState.CLOSED;
        } else if ("done".equals(issue.getStatus().getName())) {
            return IssueState.DONE;
        } else {
            return IssueState.OPEN;
        }
    }

    public static List<Issue> getJiraIssues(Scenario scenario) {
        List<String> jiraIssues = scenario.getSourceTagNames().stream().filter(t -> t.matches("^@ENTESB-\\d+$")).collect(Collectors.toList());

        if (jiraIssues.isEmpty()) {
            return Collections.emptyList();
        }

        JiraRestClient client = getJiraClient(scenario);
        IssueRestClient issueClient = client.getIssueClient();
        if (client == null) {
            return Collections.emptyList();
        }

        List<Issue> issues = new ArrayList<>();

        for (String tag : jiraIssues) {
            String issueName = tag.replaceFirst("^@", "");
            try {
                Issue issue = issueClient.getIssue(issueName).claim();
                issues.add(issue);
            } catch (RestClientException e) {
                log.error("Couldn't obtain the Jira issue : ", tag);
                e.printStackTrace();
            }
        }

        return issues;
    }

    private static JiraRestClient getJiraClient(Scenario scenario) {

        String userName = "";
        String password = "";
        String instanceUrl = "";
        URI uri = null;

        Optional<Account> account = AccountsDirectory.getInstance().getAccount(Account.Name.JIRA);
        if (account.isPresent()) {
            if (!account.get().getProperties().keySet().containsAll(Arrays.asList("username", "password", "instanceUrl"))) {
                logError(scenario,
                    "Account with name \"Jira\" and properties \"username\", \"password\", \"instanceUrl\" is required in credentials.json file.");
                logError(scenario, "If you want to get known issues from Jira in logs in case of scenario fails, update your credentials.");
                return null;
            } else {
                userName = account.get().getProperty("username");
                password = account.get().getProperty("password");
                instanceUrl = account.get().getProperty("instanceUrl");
            }
        }

        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        try {
            uri = new URI(instanceUrl);
        } catch (URISyntaxException e) {
            log.error("URL $ is a malformed URL", instanceUrl);
            e.printStackTrace();
        }
        JiraRestClient client = factory.createWithBasicHttpAuthentication(uri, userName, password);
        return client;
    }

    private static void logError(Scenario scenario, String message) {
        scenario.embed(message.getBytes(), "text/plain");
        log.error(message);
    }
}
