package io.syndesis.qe.rest.tests.integrations;

import org.springframework.beans.factory.annotation.Autowired;

import java.security.GeneralSecurityException;

import cucumber.api.java.en.When;
import io.syndesis.model.integration.Integration;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import lombok.extern.slf4j.Slf4j;

/**
 * Used for generation of integrations using the steps in StepsStorage bean.
 *
 * Jan 12, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class IntegrationHandler {

	@Autowired
	private StepsStorage steps;

	private IntegrationsEndpoint integrationsEndpoint;

	public IntegrationHandler() throws GeneralSecurityException {
		integrationsEndpoint = new IntegrationsEndpoint();
	}

	@When("^create integration with name: \"([^\"]*)\"")
	public void createIntegrationFromGivenSteps(String integrationName) throws GeneralSecurityException {

		Integration integration = new Integration.Builder()
				.steps(steps.getSteps())
				.name(integrationName)
				.desiredStatus(Integration.Status.Activated)
				.build();

		log.info("Creating integration {}", integration.getName());
		integration = integrationsEndpoint.create(integration);
		//after the integration is created - the steps are cleaned for further use.
		log.debug("Flushing used steps");
		steps.flushSteps();
	}
}
