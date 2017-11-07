package io.syndesis.qe.utils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import io.syndesis.model.connection.Action;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is used for generating of sample REST data.
 *
 * Jun 29, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public final class SampleDataGenerator {

	private SampleDataGenerator() {
	}

	public static Integration createSampleIntegration(String name) {
		return createSampleIntegration(name, Optional.empty());
	}

	public static Integration createSampleIntegration(String name, Optional<String> id) {

		final Step step1 = createSampleTwitterStep("endpoint", "test");
		final Step step2 = createSampleSalesforceStep("endpoint", "test2");

		final Integration integrationIn = new Integration.Builder()
				.name(name)
				.id(id)
				.lastUpdated(new Date(1492095344060L))
				.createdDate(new Date(1492095344060L))
				.desiredStatus(Integration.Status.Activated)
				.description("This is a test integration!")
				.steps(Arrays.asList(step1, step2))
				.build();

		return integrationIn;
	}

	public static Step createSampleSalesforceStep(String stepKind, String stepName) {

		final Step step = new SimpleStep.Builder()
				.stepKind(stepKind) //"endpoint"
				.connection(createSampleTwitterConnection(stepName))
				.configuredProperties(map("period", 5000))
				.action(createSampleSalesforceAction())
				.build();

		return step;
	}

	public static Step createSampleTwitterStep(String stepKind, String stepName) {

		final Step step = new SimpleStep.Builder()
				.stepKind(stepKind) //"endpoint"
				.connection(createSampleTwitterConnection(stepName))
				.configuredProperties(map("period", 5000))
				.action(createSampleTwitterAction())
				.build();

		return step;
	}

	public static Connection createSampleTwitterConnection(String name) {

		final Connection connection = new Connection.Builder().name(name)
				.configuredProperties(map())
				.connectorId("twitter")
				.icon("fa-globe")
				.connector(new Connector.Builder().icon("fa-globe")
						.build())
				.lastUpdated(new Date(1492095344060L))
				.createdDate(new Date(1492095344060L))
				.connectorId("twitter")
				.options(map())
				.icon("fa-twitter")
				.description("Twitter Connection")
				.build();
		return connection;
	}

	public static Connection createSampleSalesforceConnection(String name) {
		return createSampleSalesforceConnection(name, Optional.empty());
	}

	public static Connection createSampleSalesforceConnection(String name, Optional<String> id) {

		final Connection connection = new Connection.Builder()
				.name(name)
				.configuredProperties(map("loginUrl", "https://login.salesforce.com"))
				.connectorId("salesforce")
				.icon("fa-globe")
				.connector(new Connector.Builder().icon("fa-globe")
						.build())
				.lastUpdated(new Date(1492095344060L))
				.createdDate(new Date(1492095344060L))
				.connectorId("salesforce")
				.options(map())
				.icon("fa-globe")
				.id(id)
				.description("Salesforce Connection")
				.build();
		return connection;
	}

	public static Action createSampleTwitterAction() {
		final Action action = new Action.Builder()
				.camelConnectorGAV("io.syndesis:twitter-mention-connector:0.4.4")
				.description("Search for tweets that mention you")
				.camelConnectorGAV("http-get")
				.camelConnectorPrefix("twitter-mention")
				.name("Mention")
				.connectorId("io.syndesis:twitter-mention-connector:latest")
				.build();
		return action;
	}

	public static Action createSampleSalesforceAction() {
		final Action action = new Action.Builder()
				.camelConnectorGAV("io.syndesis:salesforce-create-case-connector:0.4.4")
				.description("Create a new Salesforce case based on the input message")
				.camelConnectorPrefix("salesforce-create-case")
				.name("Create Case")
				.connectorId("io.syndesis:salesforce-create-case-connector:latest")
				.build();
		return action;
	}

	public static Action connectorAction(String name) {

		final Action action = new Action.Builder()
				.camelConnectorGAV("io.syndesis:http-get-connector:0.4.4")
				.description("Call a service that is internal (within your company) or external (on the internet) by specifying the service's URL")
				.camelConnectorPrefix("http-get")
				.name(name)
				.connectorId("io.syndesis:http-get-connector:latest")
				//				.properties(map("loginUrl", "https://login.salesforce.com"))
				.build();
		return action;
	}

	private static HashMap<String, String> map(Object... values) {
		final HashMap<String, String> rc = new HashMap<>();
		for (int i = 0; i + 1 < values.length; i += 2) {
			rc.put(values[i].toString(), values[i + 1].toString());
		}
		return rc;
	}
}
