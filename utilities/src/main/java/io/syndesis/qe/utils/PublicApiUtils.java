package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;

import io.fabric8.kubernetes.api.model.ServiceAccount;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PublicApiUtils {

    private static final String SERVICE_ACCOUNT_NAME = "syndesis-cd-client";
    private static String publicApiToken;

    private PublicApiUtils() {
    }

    /**
     * Method creates service account for Public API and gives them competent role
     */
    public static void createServiceAccount() {
        ServiceAccount existServiceAccount = OpenShiftUtils.getInstance().serviceAccounts().withName(SERVICE_ACCOUNT_NAME).get();

        if (existServiceAccount == null) {
            OpenShiftUtils.getInstance().serviceAccounts()
                    .createNew()
                    .withNewMetadata()
                    .withName(SERVICE_ACCOUNT_NAME)
                    .endMetadata()
                    .done();
            OpenShiftUtils.getInstance().addRoleToUser("edit", "system:serviceaccount:" + TestConfiguration.openShiftNamespace() + ":syndesis-cd-client");
        }
        publicApiToken = OpenShiftUtils.binary().execute("sa", "get-token", SERVICE_ACCOUNT_NAME);
    }

    public static String getPublicToken() {
        if (publicApiToken == null) {
            fail("API token was not set. You have to call createServiceAccount first!");
        }
        return publicApiToken;
    }
}
