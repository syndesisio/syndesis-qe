package io.syndesis.qe.utils.jira;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Lazy
public class JiraClientFactory {

    @Bean
    public JiraRestClient getJiraRestClient() throws URISyntaxException {
        Account jiraAccount = AccountsDirectory.getInstance().get("Jira");
        String jiraUrl = jiraAccount.getProperty("jiraurl");
        return new AsynchronousJiraRestClientFactory().create(new URI(jiraUrl), new OAuthJiraAuthenticationHandler());
    }
}
