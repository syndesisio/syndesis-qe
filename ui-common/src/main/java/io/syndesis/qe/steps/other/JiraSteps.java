package io.syndesis.qe.steps.other;

import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.SyndesisRootPage;
import io.syndesis.qe.utils.TestUtils;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;

import java.util.stream.StreamSupport;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class JiraSteps {

    private static final long BUG_ISSUE_TYPE = 10006L;
    private static final int DONE_TRANSITION_TYPE = 41;

    private final JiraRestClient jiraRestClient;

    private String sharedIssueKey;

    @Autowired
    public JiraSteps(JiraRestClient jiraRestClient) {
        this.jiraRestClient = jiraRestClient;
    }

    @Given("create a new jira issue in project \"([^\"]*)\"")
    public void createNewIssue(String project) {
        IssueInputBuilder issueBuilder = new IssueInputBuilder("MTP", BUG_ISSUE_TYPE);
        issueBuilder.setSummary("test issue");
        issueBuilder.setDescription("this is the test issue");

        BasicIssue result = jiraRestClient.getIssueClient().createIssue(issueBuilder.build()).claim();
        sharedIssueKey = result.getKey();
    }

    @Then("check new comment exists in previously created jira issue with text \"([^\"]*)\"")
    public void checkCommentExists(String commentText) {
        TestUtils.waitFor(() -> commentExists(sharedIssueKey, commentText),
                1, 60,
                "Could not find comment in jira issue");
    }

    private boolean commentExists(String issueKey, String text) {
        Issue issue = jiraRestClient.getIssueClient().getIssue(issueKey).claim();

        return StreamSupport.stream(issue.getComments().spliterator(), false)
            .anyMatch(comment -> StringUtils.equalsIgnoreCase(comment.getBody().trim(), text.trim()));
    }

    @When("close previously created issue")
    public void closePreviousIssue() {
        closeIssue(sharedIssueKey);
    }

    private void closeIssue(String issueKey) {
        TransitionInput transitionInput = new TransitionInput(DONE_TRANSITION_TYPE);
        Issue issue = jiraRestClient.getIssueClient().getIssue(issueKey).claim();
        jiraRestClient.getIssueClient().transition(issue, transitionInput).claim();
    }

    @When("fill in issuekey for previously created issue")
    public void fillIssueKey() {
        Form.waitForInputs(20);
        new Form(new SyndesisRootPage().getRootElement()).fillByTestId(TestUtils.map("issuekey", sharedIssueKey));
    }

    @Then("check that open issue with summary \"([^\"]*)\" and description \"([^\"]*)\" exists")
    public void checkIssueExists(String summary, String description) {
        TestUtils.waitFor(() -> issueExists(summary, description),
                1, 60,
                "Was not able to find summary");
    }

    private boolean issueExists(String summary, String description) {
        return findIssues(summary, description).getTotal() > 0;
    }

    private SearchResult findIssues(String summary) {
        String jql = String.format("project = 'MTP' and summary ~ '%s' and status = 'To Do'",
                summary);

        return jiraRestClient.getSearchClient().searchJql(jql).claim();
    }

    private SearchResult findIssues(String summary, String description) {
        String jql = String.format("project = 'MTP' and summary ~ '%s' and description ~ '%s' and status = 'To Do'",
                summary, description);

        return jiraRestClient.getSearchClient().searchJql(jql).claim();
    }

    @When("close all issues with summary \"([^\"]*)\" and description \"([^\"]*)\"")
    public void closeAllIssuesWithSummaryAndDescription(String summary, String description) {
        for (Issue issue : findIssues(summary, description).getIssues()) {
            closeIssue(issue.getKey());
        }
    }

    @When("close all issues with summary \"([^\"]*)\"")
    public void closeAllIssuesWithSummary(String summary) {
        for (Issue issue : findIssues(summary).getIssues()) {
            closeIssue(issue.getKey());
        }
    }

    @Then("check that previously created jira is in status \"([^\"]*)\"")
    public void checkIssueStatus(String status) {
        TestUtils.waitFor(() -> checkIssueHasStatus(this.sharedIssueKey, status),
                1, 60,
                "Issue is not in the '" + status + "' status");
    }

    private boolean checkIssueHasStatus(String issueKey, String status) {
        Issue issue = jiraRestClient.getIssueClient().getIssue(issueKey).claim();
        return issue.getStatus().getName().equalsIgnoreCase(status);
    }

    @When("comment previously created jira issue with text \"([^\"]*)\"")
    public void commentIssue(String text) {
        Issue issue = jiraRestClient.getIssueClient().getIssue(sharedIssueKey).claim();
        jiraRestClient.getIssueClient().addComment(issue.getCommentsUri(), Comment.valueOf(text)).claim();
    }
}
