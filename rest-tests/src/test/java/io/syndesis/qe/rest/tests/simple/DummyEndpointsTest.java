package io.syndesis.qe.rest.tests.simple;

import static org.junit.Assert.assertTrue;

import static io.restassured.RestAssured.given;

import org.junit.Test;

import io.restassured.http.Header;
import io.restassured.response.Response;
import io.syndesis.qe.rest.tests.AbstractSyndesisRestTest;
import lombok.extern.slf4j.Slf4j;

/**
 * These endpoints don't do anything much useful for now. This class aggregates them mainly for tracking purposes.
 *
 * Jun 15, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class DummyEndpointsTest extends AbstractSyndesisRestTest {

	/**
	 * Test. GET /api/v1/connectorgroups
	 */
	@Test
	public void connectorgroupsTest() {

		final Response response = given().relaxedHTTPSValidation().header(new Header("X-Forwarded-User", "pista")).header(new Header("X-Forwarded-Access-Token", "kral"))
				.when()
				.get("/connectorgroups");

		log.debug("************ List connectorgroups ************");
		log.debug(response.asString());
		log.debug("**********************************************");
		assertTrue(Integer.parseInt(response.path("totalCount").toString()) >= 0);
	}

	/**
	 * Test. GET /api/v1/permissions
	 */
	@Test
	public void permissionsTest() {

		final Response response = given().relaxedHTTPSValidation().header(new Header("X-Forwarded-User", "pista")).header(new Header("X-Forwarded-Access-Token", "kral"))
				.when()
				.get("/permissions");

		log.debug("************** List permissions **************");
		log.debug(response.asString());
		log.debug("**********************************************");
		assertTrue(Integer.parseInt(response.path("totalCount").toString()) >= 0);
	}

	/**
	 * Test. GET /api/v1/users
	 */
	@Test
	public void usersTest() {

		final Response response = given().relaxedHTTPSValidation().header(new Header("X-Forwarded-User", "pista")).header(new Header("X-Forwarded-Access-Token", "kral"))
				.when()
				.get("/users");

		log.debug("**************** List users ******************");
		log.debug(response.asString());
		log.debug("**********************************************");
		assertTrue(Integer.parseInt(response.path("totalCount").toString()) >= 0);
	}

	/**
	 * Test. GET /api/v1/roles
	 */
	@Test
	public void rolesTest() {

		final Response response = given().relaxedHTTPSValidation().header(new Header("X-Forwarded-User", "pista")).header(new Header("X-Forwarded-Access-Token", "kral"))
				.when()
				.get("/roles");

		log.debug("**************** List roles ******************");
		log.debug(response.asString());
		log.debug("**********************************************");
		assertTrue(Integer.parseInt(response.path("totalCount").toString()) >= 0);
	}

	/**
	 * Test. GET /api/v1/tags
	 */
	@Test
	public void tagsTest() {

		final Response response = given().relaxedHTTPSValidation().header(new Header("X-Forwarded-User", "pista")).header(new Header("X-Forwarded-Access-Token", "kral"))
				.when()
				.get("/tags");

		log.debug("**************** List tags *******************");
		log.debug(response.asString());
		log.debug("**********************************************");
		assertTrue(Integer.parseInt(response.path("totalCount").toString()) >= 0);
	}
}
