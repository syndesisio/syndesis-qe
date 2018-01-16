package io.syndesis.qe.endpoints;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * TestSupport class contains utility methods for usage of the test-support endpoint.
 *
 * @author jknetl
 */
@Slf4j
public final class TestSupport {

	private static final String ENDPOINT_NAME = "/test-support";
	private static final String apiPath = TestConfiguration.syndesisRestApiPath();
	private static Client client;
	private static TestSupport instance = null;

	private TestSupport() {
		client = RestUtils.getClient();
	}

	public static TestSupport getInstance() {
		if (instance == null) {
			instance = new TestSupport();
		}
		return instance;
	}

	/**
	 * Resets Syndesis database.
	 */
	public void resetDB() {
		resetDbWithResponse();
	}

	/**
	 * Resets Syndesis database.
	 *
	 * @return HTTP response code
	 */
	public int resetDbWithResponse() {
		log.info("Resetting syndesis DB.");

		final Invocation.Builder invocation = client
				.target(getEndpointUrl())
				.request(MediaType.APPLICATION_JSON)
				.header("X-Forwarded-User", "pista")
				.header("X-Forwarded-Access-Token", "kral");
		int responseCode = invocation.get().getStatus();
		log.debug("Reset endpoint reponse: {}", responseCode);
		return responseCode;
	}

	public String getEndpointUrl() {
		return String.format("%s%s%s%s", RestUtils.getRestUrl(), apiPath, ENDPOINT_NAME, "/reset-db");
	}
}
