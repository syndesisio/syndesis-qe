package io.syndesis.qe.rest.utils;

/**
 * Constants used by syndesis REST test suite.
 * <p>
 * Jun 15, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
public final class SyndesisRestConstants {

	public static final String API_PATH = "/api/v1/";

	private SyndesisRestConstants() {
	}

	public static final String ACCOUNT_CONFIG_PATH = System.getProperty("credentials.file", "./target/test-classes/credentials.json");
	public static final String VERSIONS_CONFIG_PATH = System.getProperty("versions.file", "./target/test-classes/dependencyVersions.properties");
}
