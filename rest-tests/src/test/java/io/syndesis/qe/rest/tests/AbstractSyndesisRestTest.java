package io.syndesis.qe.rest.tests;

import io.restassured.RestAssured;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.SyndesisRestConstants;

/**
 * Abstract base for syndesis rest tests.
 *
 * Jun 26, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
public abstract class AbstractSyndesisRestTest {

	protected static String syndesisURL;

	public AbstractSyndesisRestTest() {

		syndesisURL = TestConfiguration.syndesisUrl();

		RestAssured.baseURI = syndesisURL;
		RestAssured.basePath = SyndesisRestConstants.API_PATH;
	}
}
