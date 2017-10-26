package io.syndesis.qe.rest.utils;

import java.nio.file.Paths;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.syndesis.qe.rest.accounts.Account;
import io.syndesis.qe.rest.accounts.AccountsDirectory;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenShift utils.
 *
 * Sep 8, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public final class OpenShiftUtils {

	private static OpenShiftClient client;
	private static AccountsDirectory accountsDirectory;
	private static Account openshiftAccount;

	private static OpenShiftUtils openShiftUtils = null;

	public static OpenShiftUtils getInstance() {
		if (openShiftUtils == null) {
			openShiftUtils = new OpenShiftUtils();
		}
		return openShiftUtils;
	}

	private OpenShiftUtils() {
		accountsDirectory = new AccountsDirectory(Paths.get(SyndesisRestConstants.ACCOUNT_CONFIG_PATH));
		openshiftAccount = accountsDirectory.getAccount("openshift").get();
		final Account syndesisAccount = accountsDirectory.getAccount("syndesis").get();

		final Config config = new ConfigBuilder()
				.withUsername(syndesisAccount.getProperty("login"))
				.withPassword(syndesisAccount.getProperty("password"))
				.withMasterUrl(openshiftAccount.getProperty("instanceUrl"))
				.build();

		client = new DefaultOpenShiftClient(config);
	}

	/**
	 * Get the openshift user ID by OS user name.
	 *
	 * @param userName
	 * @return
	 */
	public static String getOpenShiftUserId(String userName) {

		String userUid = "";
		final User user = client.currentUser();
		final ObjectMeta metadata = user.getMetadata();
		if (openshiftAccount.getProperty("UID") != null && !openshiftAccount.getProperty("UID").isEmpty()) {
			userUid = openshiftAccount.getProperty("UID");
		} else {
			userUid = metadata.getUid();
		}
		return userUid;
	}
}
