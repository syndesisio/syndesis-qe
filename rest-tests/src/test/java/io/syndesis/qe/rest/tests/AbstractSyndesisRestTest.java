package io.syndesis.qe.rest.tests;

import java.nio.file.Paths;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.restassured.RestAssured;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.SyndesisRestConstants;

/**
 * Abstract base for syndesis rest tests.
 *
 * Jun 26, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
public abstract class AbstractSyndesisRestTest {

	private static Account syndesisAccount;
	private static Account openshiftAccount;
	private static AccountsDirectory accountsDirectory;

	protected static String syndesisURL;
	protected static String openshiftToken;
	protected static String openshiftUrl;
	protected static OpenShiftClient openshiftClient;

	public AbstractSyndesisRestTest() {

		accountsDirectory = new AccountsDirectory(Paths.get(SyndesisRestConstants.ACCOUNT_CONFIG_PATH));

		syndesisAccount = accountsDirectory.getAccount("syndesis").get();
		openshiftAccount = accountsDirectory.getAccount("openshift").get();

		syndesisURL = syndesisAccount.getProperty("instanceUrl");
		openshiftUrl = openshiftAccount.getProperty("instanceUrl");
		openshiftToken = openshiftAccount.getProperty("openshiftToken");
		RestAssured.baseURI = syndesisURL;
		RestAssured.basePath = SyndesisRestConstants.API_PATH;

		final Config config = new ConfigBuilder()
				.withUsername(syndesisAccount.getProperty("login"))
				.withPassword(syndesisAccount.getProperty("password"))
				.withMasterUrl(openshiftUrl)
				.build();

		openshiftClient = new DefaultOpenShiftClient(config);
	}
}
