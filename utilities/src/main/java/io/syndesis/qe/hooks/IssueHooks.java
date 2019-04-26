package io.syndesis.qe.hooks;

import org.junit.Assume;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.issue.IssueState;
import io.syndesis.qe.issue.SimpleIssue;
import io.syndesis.qe.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Idea of these hooks: we can maintain currently open issues which causes tests to fail
 * and simultaneously history of resolved issues per scenario on one place for easier
 * fail investigation.
 */
@Slf4j
public class IssueHooks {

    /**
     * This hook skips tests that have open github issues
     * <p>
     * Only runs when {@link TestConfiguration#SKIP_TESTS_WITH_OPEN_ISSUES} property is set to true
     *
     * @param scenario
     */
    @Before
    public void skipTestsWithOpenIssues(Scenario scenario) {
        if (TestConfiguration.skipTestsWithOpenIssues()) {
            log.info(scenario.getName());

            List<Issue> issues = getIssues(scenario);

            for (Issue issue : issues) {
                // assumeFalse will skip the test if the argument evaluates to true, i.e. when the issue is open
                Assume.assumeFalse(IssueState.OPEN.equals(getIssueState(scenario, issue)));
            }
        }
    }

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

        List<Issue> issues = getIssues(scenario);

        if (issues.isEmpty()) {
            logError(scenario, "############ No GitHub issue annotations found ############");
            return;
        }

        try {
            List<Issue> openIssues = new ArrayList<>();
            List<Issue> doneIssues = new ArrayList<>();
            List<Issue> closedIssues = new ArrayList<>();

            for (Issue issue : issues) {

                switch (getIssueState(scenario, issue)) {
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

            logError(scenario, "############ FAILED PROBABLY DUE TO: ################");
            logError(scenario, "######## DONE issues ########");
            logIssues(scenario, doneIssues);
            logError(scenario, "######## CLOSED issues ########");
            logIssues(scenario, closedIssues);
            logError(scenario, "######## OPEN issues ########");
            logIssues(scenario, openIssues);
            embedIssues(scenario, issues);
        } catch (Exception e) {
            log.error("Error while processing GitHub issues", e);
            scenario.embed("Error while processing GitHub issues".getBytes(), "text/plain");
            e.printStackTrace();
        }
    }

    private void embedIssues(Scenario scenario, List<Issue> issues) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode array = mapper.createArrayNode();

        for (Issue issue : issues) {
            array.addPOJO(new SimpleIssue(issue.getNumber(), issue.getUrl(), getIssueState(scenario, issue)));
        }

        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, array);
        scenario.embed(sw.toString().getBytes(), "application/x.issues+json");
    }

    public static IssueState getIssueState(Scenario scenario, Issue issue) {
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

    public static List<Issue> getIssues(Scenario scenario) {
        List<String> ghIssues = scenario.getSourceTagNames().stream().filter(t -> t.matches("^@gh-\\d+$")).collect(Collectors.toList());

        if (ghIssues.isEmpty()) {
            return Collections.emptyList();
        }

        GitHubClient client = getGitHubClient(scenario);
        if (client == null) {
            return Collections.emptyList();
        }

        RepositoryService repositoryService = new RepositoryService(client);
        IssueService issueService = new IssueService(client);

        List<Issue> issues = new ArrayList<>();
        try {
            Repository repository = repositoryService.getRepository("syndesisio", "syndesis");

            for (String tag : ghIssues) {
                String issueNumber = tag.replaceFirst("^@gh-", "");
                Issue issue = issueService.getIssue(repository, issueNumber);
                issues.add(issue);
            }
        } catch (IOException e) {
            log.error("Error while processing GitHub issues", e);
            scenario.embed("Error while processing GitHub issues".getBytes(), "text/plain");
            e.printStackTrace();
        }

        return issues;
    }

    private static void logIssues(Scenario scenario, List<Issue> issues) {
        for (Issue issue : issues) {
            logError(scenario, "#### Title: " + issue.getTitle());
            logError(scenario, "#### Link: " + issue.getHtmlUrl());
            logError(scenario, "----------------------------------------");
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

    private static void logError(Scenario scenario, String message) {
        scenario.embed(message.getBytes(), "text/plain");
        log.error(message);
    }
}

