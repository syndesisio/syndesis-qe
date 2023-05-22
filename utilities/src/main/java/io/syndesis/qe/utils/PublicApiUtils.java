package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.TestConfiguration;

import org.bouncycastle.util.encoders.Base64;

import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
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
        ServiceAccount existServiceAccount =
            OpenShiftUtils.getInstance().serviceAccounts().withName(SERVICE_ACCOUNT_NAME).get();

        if (existServiceAccount == null) {
            OpenShiftUtils.getInstance().serviceAccounts().create(new ServiceAccountBuilder()
                .withNewMetadata()
                .withName(SERVICE_ACCOUNT_NAME)
                .endMetadata()
                .build());

            OpenShiftUtils.getInstance().addRoleToUser("edit",
                "system:serviceaccount:" + TestConfiguration.openShiftNamespace() + ":syndesis-cd-client");
        }
        publicApiToken = new String(
            Base64.decode(OpenShiftUtils.getInstance().secrets().list().getItems().stream()
                .filter(secret -> secret.getMetadata().getName().contains(SERVICE_ACCOUNT_NAME + "-token")).findFirst()
                .get().getData().get("token")));
    }

    public static String getPublicToken() {
        if (publicApiToken == null) {
            fail("API token was not set. You have to call createServiceAccount first!");
        }
        return publicApiToken;
    }
}
