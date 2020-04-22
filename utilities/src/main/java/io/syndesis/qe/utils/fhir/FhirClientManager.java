package io.syndesis.qe.utils.fhir;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ca.uhn.fhir.context.FhirContext;
import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FhirClientManager {
    private static LocalPortForward LOCAL_PORT_FORWARD = null;

    private final int fhirLocalPort = 8082;
    private String fhirServerLocalUrl;
    private String fhirPodName;
    private int fhirRemotePort;

    @Getter
    private FhirContext ctx  = FhirContext.forDstu3();

    public FhirClientManager() {
        initProperties();
    }

    public MyPatientClient getClient() {

        if (LOCAL_PORT_FORWARD == null || !LOCAL_PORT_FORWARD.isAlive()) {
            LOCAL_PORT_FORWARD =
                OpenShiftUtils.portForward(OpenShiftUtils.getInstance().getAnyPod("app", fhirPodName), fhirRemotePort, fhirLocalPort);
        }
        return initClient();
    }

    public static void closeClient() {
        TestUtils.terminateLocalPortForward(LOCAL_PORT_FORWARD);
    }

    private MyPatientClient initClient() {
//        ctx = FhirContext.forDstu3();
        return ctx.newRestfulClient(MyPatientClient.class, fhirServerLocalUrl);
    }

    private void initProperties() {
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(Account.Name.FHIR);
        if (optional.isPresent()) {
            Map<String, String> properties = new HashMap<>();
            optional.get().getProperties().forEach((key, value) ->
                properties.put(key.toLowerCase(), value)
            );
            fhirPodName = properties.get("host");
            fhirRemotePort = Integer.parseInt(properties.get("port"));
            fhirServerLocalUrl = "http://localhost:" + fhirLocalPort + "/baseDstu3";
        } else {
            Assert.fail("Credentials for " + Account.Name.FHIR + " were not found!");
        }
    }
}
