package io.syndesis.qe.steps.connections.detail;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;

import cucumber.api.java.en.Then;
import io.syndesis.qe.pages.connections.detail.ConnectionDetail;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DetailSteps {

    private ConnectionDetail detailPage = new ConnectionDetail();

    @Then("^check visibility of \"([^\"]*)\" connection details")
    public void verifyConnectionDetails(String connectionName) {
        log.info("Connection detail page must show connection name");
        assertThat(detailPage.connectionName(), is(connectionName));
    }
}
