package io.syndesis.qe.hooks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * Idea of these hooks: we can maintain currently open issues which causes tests to fail
 * and simultaneously history of resolved issues per scenario on one place for easier
 * fail investigation.
 */
@Slf4j
public class OnFailHooks {

    /**
     * This hook checks and reports status of linked github issues.
     * <p>
     * Each failed scenario is checked if it contains a tag in the form @gh-&lt;issue-number&gt;.
     * If it does, GitHub and ZenHub are queried for the status of each issue and a summary is written into log and cucumber report.
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
    public void checkGitHubIssues(Scenario scenario) {
        if (!scenario.isFailed()) {
            return;
        }

        List<String> ghIssues = scenario.getSourceTagNames().stream().filter(t -> t.matches("^@gh-\\d+$")).collect(Collectors.toList());

        if (ghIssues.isEmpty()) {
            logError(scenario, "############ No GitHub issue annotations found ############");
            return;
        }

        GitHubClient client = getGitHubClient(scenario);
        if (client == null) {
            return;
        }
        RepositoryService repositoryService = new RepositoryService(client);
        IssueService issueService = new IssueService(client);

        try {
            Repository repository = repositoryService.getRepository("syndesisio", "syndesis");

            List<Issue> openIssues = new ArrayList<>();
            List<Issue> doneIssues = new ArrayList<>();
            List<Issue> closedIssues = new ArrayList<>();

            for (String tag : ghIssues) {
                String issueNumber = tag.replaceFirst("^@gh-", "");
                Issue issue = issueService.getIssue(repository, issueNumber);

                if ("open".equals(issue.getState())) {
                    // need to get issue from zenhub to determine the pipeline status
                    String zenHubPipeline = getZenHubPipeline(scenario, issueNumber);
                    if ("Done".equals(zenHubPipeline)) {
                        doneIssues.add(issue);
                    } else {
                        openIssues.add(issue);
                    }
                } else if ("closed".equals(issue.getState())) {
                    closedIssues.add(issue);
                }
            }

            logError(scenario, "############ FAILED PROBABLY DUE TO: ################");
            logError(scenario, "######## DONE issues ########");
            logIssues(scenario, doneIssues);
            logError(scenario, "######## CLOSED issues ########");
            logIssues(scenario, closedIssues);
            logError(scenario, "######## OPEN issues ########");
            logIssues(scenario, openIssues);
        } catch (IOException e) {
            log.error("Error while processing GitHub issues", e);
            scenario.embed("Error while processing GitHub issues".getBytes(), "text/plain");
            e.printStackTrace();
        }
    }


    private void logIssues(Scenario scenario, List<Issue> issues) {
        for (Issue issue : issues) {
            logError(scenario, "#### Title: " + issue.getTitle());
            logError(scenario, "#### Link: " + issue.getHtmlUrl());
            logError(scenario, "----------------------------------------");
        }
    }

    private GitHubClient getGitHubClient(Scenario scenario) {
        String oauthToken = "";

        Optional<Account> optional = AccountsDirectory.getInstance().getAccount("GitHub");
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


    private String getZenHubPipeline(Scenario scenario, String issueNumber) {
        // TODO: this whole thing should probably be refactored eventually
        String oauthToken = "";

        Optional<Account> optional = AccountsDirectory.getInstance().getAccount("ZenHub");
        if (optional.isPresent()) {
            if (!optional.get().getProperties().containsKey("APIToken")) {
                logError(scenario, "Account with name \"ZenHub\" and property \"APIToken\" is required in credentials.json file.");
                return null;
            } else {
                oauthToken = optional.get().getProperty("APIToken");
            }
        }

        Client client = RestUtils.getClient();
        // hardcoded syndesis repo id for now
        JsonNode jsonNode = client.target("https://api.zenhub.io/p1/repositories/105563335/issues/" + issueNumber)
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

    private void logError(Scenario scenario, String message) {
        scenario.embed(message.getBytes(), "text/plain");
        log.error(message);
    }
}

