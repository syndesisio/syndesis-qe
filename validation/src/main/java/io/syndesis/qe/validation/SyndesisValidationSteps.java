package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.endpoint.ConnectionsEndpoint;
import io.syndesis.qe.endpoint.ExtensionsEndpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import io.cucumber.java.en.Then;

public class SyndesisValidationSteps {
    @Autowired
    @Lazy
    private ConnectionsEndpoint connections;

    @Autowired
    @Lazy
    private ExtensionsEndpoint extensions;

    @Then("check that connection {string} exists")
    public void checkConnection(String connection) {
        assertThat(connections.getConnectionByName(connection)).isNotNull();
    }

    @Then("check that connection {string} doesn't exist")
    public void checkConnectionDoesNotExist(String connection) {
        assertThat(connections.getConnectionByName(connection)).isNull();
    }

    @Then("check that extension {string} exists")
    public void checkExtension(String extension) {
        // This fails when it is not present, so we don't need the value
        extensions.getExtensionByName(extension);
    }
}
