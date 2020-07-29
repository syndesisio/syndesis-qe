package io.syndesis.qe.utils;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.endpoint.client.EndpointClient;
import io.syndesis.qe.issue.IssueState;
import io.syndesis.qe.issue.SimpleIssue;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IssueHooksUtils {

    public static List<SimpleIssue> analyzeJiraIssues(List<String> jiraIssues, Scenario scenario) {

        List<SimpleIssue> issues = new ArrayList<>();
        JiraRestClient jiraClient = getJiraClient(scenario);

        if (jiraClient == null) {
            return Collections.emptyList();
        }
        IssueRestClient issueClient = jiraClient.getIssueClient();

        for (String tag : jiraIssues) {
            String issueName = tag.replaceFirst("^@", "");
            try {
                com.atlassian.jira.rest.client.api.domain.Issue issue = issueClient.getIssue(issueName).claim();
                issues.add(transformJiraIssue(issue));
            } catch (RestClientException e) {
                log.error("Couldn't obtain the Jira issue : {}", tag);
                scenario.attach("Error while processing Jira issues".getBytes(), "text/plain", "ErrorMessage");
                e.printStackTrace();
            }
        }

        return issues;
    }

    public static List<SimpleIssue> analyzeGithubIssues(List<String> githubIssues, Scenario scenario) {

        GitHubClient gitHubClient = getGitHubClient(scenario);
        List<SimpleIssue> issues = new ArrayList<>();

        if (gitHubClient == null) {
            return Collections.emptyList();
        }

        RepositoryService repositoryService = new RepositoryService(gitHubClient);
        IssueService issueService = new IssueService(gitHubClient);

        try {
            Repository repository = repositoryService.getRepository("syndesisio", "syndesis");

            for (String tag : githubIssues) {
                String issueNumber = tag.replaceFirst("^@gh-", "");
                Issue issue = issueService.getIssue(repository, issueNumber);
                issues.add(transformGithubIssue(issue, scenario));
            }
        } catch (IOException e) {
            log.error("Error while processing GitHub issues", e);
            scenario.attach("Error while processing GitHub issues".getBytes(), "text/plain", "ErrorMessage");
            e.printStackTrace();
        }

        return issues;
    }

    private static SimpleIssue transformGithubIssue(Issue githubIssue, Scenario scenario) {

        SimpleIssue issue = new SimpleIssue();

        issue.setIssue(Integer.toString(githubIssue.getNumber()));
        issue.setUrl(githubIssue.getHtmlUrl());
        issue.setIssueSummary(githubIssue.getTitle());
        issue.setState(getGHIssueState(scenario, githubIssue));

        return issue;
    }

    private static SimpleIssue transformJiraIssue(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {

        SimpleIssue issue = new SimpleIssue();

        issue.setIssue(jiraIssue.getKey());
        issue.setUrl(getJiraIssueUrl(jiraIssue));
        issue.setIssueSummary(jiraIssue.getSummary());
        issue.setState(getJiraIssueState(jiraIssue));

        return issue;
    }

    private static IssueState getGHIssueState(Scenario scenario, Issue issue) {
        if ("open".equals(issue.getState())) {
            // need to get issue from zenhub to determine the pipeline status
            String zenHubPipeline = getZenHubPipeline(scenario, String.valueOf(issue.getNumber()));
            if ("Done".equals(zenHubPipeline)) {
                return IssueState.DONE;
            } else {
                return IssueState.OPEN;
            }
        } else if ("closed".equals(issue.getState())) {
            return IssueState.CLOSED;
        }
        throw new IllegalArgumentException("Unknown issue state " + issue.getState());
    }

    /**
     * Issues in ENTESB contain several states that are covered by the umbrella "Open" state:
     * "Triage backlog", "In clarification", "Release backlog", "Sprint backlog", "Validation failed", "In development", "In review"
     * <p>
     * The "Done" state also represents several different states, namely:
     * "Productization backlog", "In productization", "Validation backlog", "In Validation", "Done"
     * <p>
     * The "Closed" state represents single state - "Closed"
     *
     * @param issue jira issue
     * @return issue state
     */
    private static IssueState getJiraIssueState(com.atlassian.jira.rest.client.api.domain.Issue issue) {

        for (IssueState.DoneJira d : IssueState.DoneJira.values()) {
            if (d.getId().equals(issue.getStatus().getName())) {
                return IssueState.DONE;
            }
        }

        if ("Closed".equals(issue.getStatus().getName())) {
            return IssueState.CLOSED;
        } else {
            return IssueState.OPEN;
        }
    }

    private static String getJiraIssueUrl(com.atlassian.jira.rest.client.api.domain.Issue jiraIssue) {
        return AccountsDirectory.getInstance().get(Account.Name.JIRA_HOOK).getProperty("instanceUrl") + "/browse/" + jiraIssue.getKey();
    }

    private static String getZenHubPipeline(Scenario scenario, String issueNumber) {
        // TODO: this whole thing should probably be refactored eventually
        String oauthToken = "";

        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(Account.Name.ZENHUB);
        if (optional.isPresent()) {
            if (!optional.get().getProperties().containsKey("APIToken")) {
                logError(scenario, "Account with name \"ZenHub\" and property \"APIToken\" is required in credentials.json file.");
                return null;
            } else {
                oauthToken = optional.get().getProperty("APIToken");
            }
        }

        // hardcoded syndesis repo id for now
        JsonNode jsonNode = EndpointClient.getClient().target("https://api.zenhub.io/p1/repositories/105563335/issues/" + issueNumber)
            .request(MediaType.APPLICATION_JSON)
            .header("X-Authentication-Token", oauthToken)
            .get(JsonNode.class);

        if (jsonNode != null &&
            jsonNode.has("pipeline") &&
            jsonNode.get("pipeline").has("name")) {
            return jsonNode.get("pipeline").get("name").asText();
        } else {
            logError(scenario, "No ZenHub pipeline info found for issue " + issueNumber);
            return null;
        }
    }

    private static GitHubClient getGitHubClient(Scenario scenario) {
        String oauthToken = "";

        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(Account.Name.GITHUB);
        if (optional.isPresent()) {
            if (!optional.get().getProperties().containsKey("PersonalAccessToken")) {
                logError(scenario, "Account with name \"GitHub\" and property \"PersonalAccessToken\" is required in credentials.json file.");
                logError(scenario, "If you want to get known issues from github in logs in case of scenario fails, update your credentials.");
                return null;
            } else {
                oauthToken = optional.get().getProperty("PersonalAccessToken");
            }
        }

        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(oauthToken);
        return client;
    }

    private static JiraRestClient getJiraClient(Scenario scenario) {
        String userName = "";
        String password = "";
        String instanceUrl = "";
        URI uri = null;

        Optional<Account> account = AccountsDirectory.getInstance().getAccount(Account.Name.JIRA_HOOK);
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
            log.error("URL {} is a malformed URL", instanceUrl);
            e.printStackTrace();
        }
        return factory.createWithBasicHttpAuthentication(uri, userName, password);
    }

    public static void logError(Scenario scenario, String message) {
        scenario.attach(message.getBytes(), "text/plain", "ErrorMessage");
        log.error(message);
    }
}
