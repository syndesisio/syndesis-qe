package io.syndesis.qe.utils;

import cz.xtf.openshift.OpenShiftBinaryClient;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.syndesis.qe.TestConfiguration;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.fail;

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
        ServiceAccount existServiceAccount = OpenShiftUtils.client().serviceAccounts().withName(SERVICE_ACCOUNT_NAME).get();

        if (existServiceAccount == null) {
            OpenShiftUtils.client().serviceAccounts()
                    .createNew()
                    .withNewMetadata()
                    .withName(SERVICE_ACCOUNT_NAME)
                    .endMetadata()
                    .done();
            OpenShiftUtils.xtf().addRoleToUser("edit", "system:serviceaccount:syndesis:syndesis-cd-client");
        }
        OpenShiftBinaryClient.getInstance().project(TestConfiguration.openShiftNamespace());
        publicApiToken = OpenShiftBinaryClient.getInstance().executeCommandWithReturn("", "sa", "get-token", SERVICE_ACCOUNT_NAME);
    }

    public static String getPublicToken() {
        if (publicApiToken == null) {
            fail("API token was not set. You have to call createServiceAccount first!");
        }
        return publicApiToken;
    }
}
