package io.syndesis.qe.templates;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPEndpointsTemplate {
    private static final String TEMPLATE_URL = "https://raw.githubusercontent.com/avano/HTTPEndpoints/master/template.yml";

    public static void deploy() {
        if (!TestUtils.isDcDeployed("httpendpoints")) {
            try {
                OpenShiftUtils.client().load(new URL(TEMPLATE_URL).openStream()).createOrReplace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                OpenShiftWaitUtils.waitFor(OpenShiftWaitUtils.isAPodReady("app", "httpendpoints"));
            } catch (InterruptedException | TimeoutException e) {
                log.error("Wait for http endpoints failed ", e);
            }
        }
        addAccounts();
    }

    private static void addAccounts() {
        Account http = new Account();
        Map<String, String> params = new HashMap<>();
        params.put("baseUrl", "http://http-svc:8080");
        http.setService("http");
        http.setProperties(params);
        AccountsDirectory.getInstance().getAccounts().put("http", http);
        Account https = new Account();
        params = new HashMap<>();
        params.put("baseUrl", "https://https-svc:8443");
        https.setService("https");
        https.setProperties(params);
        AccountsDirectory.getInstance().getAccounts().put("https", https);
    }
}
