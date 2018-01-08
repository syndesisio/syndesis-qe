package io.syndesis.qe.validation;

import org.assertj.core.api.Assertions;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cucumber.api.java.en.Then;
import io.syndesis.model.integration.Integration;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Dec 12, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class CommonValidationSteps {

	private final IntegrationsEndpoint integrationsEndpoint;

	public CommonValidationSteps() throws GeneralSecurityException {
		integrationsEndpoint = new IntegrationsEndpoint(RestConstants.getInstance().getSyndesisURL());
	}

	@Then("^wait for integration with name: \"([^\"]*)\" to become active")
	public void waitForIntegrationToBeActive(String integrationName) {
		final List<Integration> integrations = integrationsEndpoint.list().stream()
				.filter(item -> item.getName().equals(integrationName))
				.collect(Collectors.toList());

		final long start = System.currentTimeMillis();
		//wait for activation
		log.info("Waiting until integration \"{}\" becomes active. This may take a while...", integrationName);
		final boolean activated = TestUtils.waitForActivation(integrationsEndpoint, integrations.get(0), TimeUnit.MINUTES, 10);
		Assertions.assertThat(activated).isEqualTo(true);
		log.info("Integration pod has been started. It took {}s to build the integration.", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));
	}
}
