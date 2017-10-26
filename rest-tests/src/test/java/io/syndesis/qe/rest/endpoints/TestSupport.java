package io.syndesis.qe.rest.endpoints;

import static io.restassured.RestAssured.given;

import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;

/**
 * TestSupport class contains utility methods for usage of the test-support endpoint.
 *
 * @author jknetl
 */
@Slf4j
public final class TestSupport {

	public static final String ENDPOINT_NAME = "test-support";

	private TestSupport() {
	}

	/**
	 * Resets syndesis database.
	 *
	 * @param token
	 */
	public static void resetDB() {
		log.info("Resetting syndesis DB.");
		given().relaxedHTTPSValidation().header(new Header("X-Forwarded-User", "pista")).header(new Header("X-Forwarded-Access-Token", "kral"))
				.when()
				.get(ENDPOINT_NAME + "/reset-db")
				.asString();
	}
}
