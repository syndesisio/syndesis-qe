package io.syndesis.qe.util.fhir;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.utils.OpenShiftUtils;

import java.util.HashMap;
import java.util.Map;

import ca.uhn.fhir.context.FhirContext;
import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FhirClientManager {
    private static FhirClientManager instance = null;
    private static LocalPortForward LOCAL_PORT_FORWARD = null;

    private final int fhirLocalPort = 8082;
    private String fhirServerLocalUrl;
    private String fhirPodName;
    private int fhirRemotePort;

    @Getter
    private FhirContext ctx  = FhirContext.forDstu3();

    private FhirClientManager() {
        initProperties();
    }

    public static FhirClientManager getInstance() {
        if (instance == null) {
            instance = new FhirClientManager();
        }
        return instance;
    }

    public MyPatientClient getClient() {

        if (LOCAL_PORT_FORWARD == null || !LOCAL_PORT_FORWARD.isAlive()) {
            LOCAL_PORT_FORWARD =
                OpenShiftUtils.portForward(OpenShiftUtils.getInstance().getAnyPod("app", fhirPodName), fhirRemotePort, fhirLocalPort);
        }
        return initClient();
    }

    public static void closeClient() {
        OpenShiftUtils.terminateLocalPortForward(LOCAL_PORT_FORWARD);
    }

    private MyPatientClient initClient() {
        return ctx.newRestfulClient(MyPatientClient.class, fhirServerLocalUrl);
    }

    private void initProperties() {
        Account account = AccountsDirectory.getInstance().get(Account.Name.FHIR);
        Map<String, String> properties = new HashMap<>();
        account.getProperties().forEach((key, value) ->
            properties.put(key.toLowerCase(), value)
        );
        fhirPodName = properties.get("host");
        fhirRemotePort = Integer.parseInt(properties.get("port"));
        fhirServerLocalUrl = "http://localhost:" + fhirLocalPort + "/baseDstu3";
    }
}
