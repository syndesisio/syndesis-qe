package io.syndesis.qe.templates;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
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
        }
        addAccount();
    }

    private static void addAccount() {
        Account http = new Account();
        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("baseUrlHttp", "http://http-svc:8080");
        httpParams.put("baseUrlHttps", "https://https-svc:8443");
        http.setService("http");
        http.setProperties(httpParams);
        AccountsDirectory.getInstance().getAccounts().put("http", http);
    }
}
