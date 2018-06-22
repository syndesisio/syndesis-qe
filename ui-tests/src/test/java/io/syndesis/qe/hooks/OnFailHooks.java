package io.syndesis.qe.hooks;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Idea of these hooks: we can maintain currently open issues which causes tests to fail
 * and simultaneously history of resolved issues per scenario on one place for easier
 * fail investigation.
 */
@Slf4j
public class OnFailHooks {

    @After
    public void invokeLibrary(Scenario scenario) {
        if (!scenario.isFailed()) {
            return;
        }

        String oauthToken = "";

        Optional<Account> optional = AccountsDirectory.getInstance().getAccount("GitHub");
        if (optional.isPresent()) {
            if (!optional.get().getProperties().containsKey("PersonalAccessToken")) {
                scenario.embed("Account with name \"GitHub\" and property \"PersonalAccessToken\" is required in credentials.json file.".getBytes(), "text/plain");
                log.error("Account with name \"GitHub\" and property \"PersonalAccessToken\" is required in credentials.json file.");
                scenario.embed("If you want to get known issues from github in logs in case of scenario fails, update your credentials.".getBytes(), "text/plain");
                log.error("If you want to get known issues from github in logs in case of scenario fails, update your credentials.");
                return;
            } else {
                oauthToken = optional.get().getProperty("PersonalAccessToken");
            }
        }

        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(oauthToken);
        RepositoryService repositoryService = new RepositoryService(client);
        IssueService issueService = new IssueService(client);

        try {
            scenario.embed("############ FAILED PROBABLY DUE TO: ################".getBytes(), "text/plain");
            log.error("############ FAILED PROBABLY DUE TO: ################");

            //iterate through all tags
            for (String tag : scenario.getSourceTagNames()) {
                Map<String, String> filter = new HashMap<>();
                scenario.embed(("######## Open Issues for scenario: " + tag).getBytes(), "text/plain");
                log.error("######## Open Issues for scenario: " + tag);
                filter.put("labels", "qe/" + tag.substring(1));
                //for some reason this request returns only open issues - probably default github setting
                List<Issue> reportedIssues = issueService.getIssues(repositoryService.getRepository("syndesisio", "syndesis"), filter);
                filter.put("state", "closed");
                List<Issue> closedIssues = issueService.getIssues(repositoryService.getRepository("syndesisio", "syndesis"), filter);
                if (reportedIssues.size() == 0) {
                    scenario.embed("#### No previously reported issues found.".getBytes(), "text/plain");
                    log.error("#### No previously reported issues found.");
                    continue;
                }

                //print all open issues, if closed save to print later so it is sorted open/closed
                for (Issue knownIssue : reportedIssues) {
                    if (knownIssue.getState().equalsIgnoreCase("open")) {
                        scenario.embed(("#### Title: " + knownIssue.getTitle()).getBytes(), "text/plain");
                        log.error("#### Title: " + knownIssue.getTitle());
                        scenario.embed(("#### Link: " + knownIssue.getHtmlUrl()).getBytes(), "text/plain");
                        log.error("#### Link: " + knownIssue.getHtmlUrl());
                        scenario.embed("#### ------------------------------------------------------------------".getBytes(), "text/plain");
                        log.error("#### ------------------------------------------------------------------");
                    } else {
                        closedIssues.add(knownIssue);
                    }
                }
                if (closedIssues.size() == 0) {
                    continue;
                }

                scenario.embed(("######## Resolved issues for tag: " + tag).getBytes(), "text/plain");
                log.error("######## Resolved issues for tag: " + tag);
                //print closed issues
                for (Issue closedIssue : closedIssues) {
                    scenario.embed(("#### Title: " + closedIssue.getTitle()).getBytes(), "text/plain");
                    log.error("#### Title: " + closedIssue.getTitle());
                    scenario.embed(("#### Link: " + closedIssue.getHtmlUrl()).getBytes(), "text/plain");
                    log.error("#### Link: " + closedIssue.getHtmlUrl());
                    scenario.embed("----------------------------------------------------------------------".getBytes(), "text/plain");
                    log.error("----------------------------------------------------------------------");
                }
            }
        } catch (IOException e) {
            log.error("Error while processing GitHub issues", e);
            scenario.embed("Error while processing GitHub issues".getBytes(), "text/plain");
            e.printStackTrace();
        }
    }
}
