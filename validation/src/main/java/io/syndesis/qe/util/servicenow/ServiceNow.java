package io.syndesis.qe.util.servicenow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.util.servicenow.model.Incident;
import io.syndesis.qe.util.servicenow.model.IncidentRecordList;
import io.syndesis.qe.util.servicenow.model.IncidentSingleResponse;
import io.syndesis.qe.utils.http.HTTPResponse;
import io.syndesis.qe.utils.http.HTTPUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;

/**
 * Simple request wrapper for Service-Now incidents API.
 */
@Slf4j
public class ServiceNow {
    private static final String INCIDENT_ENDPOINT = "api/now/v2/table/incident";
    private static ServiceNow instance = new ServiceNow();
    private static String url;
    private static String userName;
    private static String password;
    private static ObjectMapper om;

    public static ServiceNow getInstance() {
        return instance;
    }

    private ServiceNow() {
        Optional<Account> serviceNow = AccountsDirectory.getInstance().getAccount(Account.Name.SERVICENOW);
        assertThat(serviceNow).isPresent();
        String instanceName = serviceNow.get().getProperty("instanceName");
        userName = serviceNow.get().getProperty("userName");
        password = serviceNow.get().getProperty("password");
        url = String.format("https://%s.service-now.com/%s", instanceName, INCIDENT_ENDPOINT);
        om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static Incident createIncident(Incident i) {
        String json = getIncidentJson(i);
        log.debug(String.format("Sending incident Json: %s", json));

        HTTPResponse r = HTTPUtils.doPostRequest(
            url,
            json,
            "application/json",
            Headers.of("Authorization", "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes()))
        );

        assertThat(r.getCode()).isEqualTo(201);

        return getIncidentFromResponse(r);
    }

    public static List<Incident> getIncidents(int limit) {
        return getFilteredIncidents(null, limit);
    }

    public static List<Incident> getFilteredIncidents(String filter, int limit) {
        String getUrl = url + "?sysparm_limit=" + limit;
        if (filter != null) {
            try {
                getUrl += "&sysparm_query=" + URLEncoder.encode(filter, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // this won't happen
            }
        }

        HTTPResponse r = HTTPUtils.doGetRequest(
            getUrl,
            Headers.of("Authorization", "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes()))
        );

        assertThat(r.getCode()).isEqualTo(200);

        String responseBody = r.getBody();
        return parseResponse(responseBody);
    }

    public static void deleteIncident(String sysId) {
        HTTPResponse r = HTTPUtils.doDeleteRequest(
            String.format("%s/%s", url, sysId),
            Headers.of("Authorization", "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes()))
        );

        assertThat(r.getCode()).isEqualTo(204);
    }

    public static Incident updateIncident(String sysId, Incident incident) {
        String json = getIncidentJson(incident);
        log.debug(String.format("Sending incident Json: %s", json));

        HTTPResponse r = HTTPUtils.doPutRequest(
            String.format("%s/%s", url, sysId),
            json,
            "application/json",
            Headers.of("Authorization", "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes()))
        );

        assertThat(r.getCode()).isEqualTo(200);

        return getIncidentFromResponse(r);
    }

    /**
     * Service-Now API returns single object when there is 1 record, but list of records when there are more records.
     *
     * @param response json response
     * @return list of records
     */
    private static List<Incident> parseResponse(String response) {
        List<Incident> incidents = new ArrayList<>();
        try {
            IncidentRecordList irl = om.readValue(response, IncidentRecordList.class);
            incidents.addAll(irl.getRecords());
            return incidents;
        } catch (IOException e) {
            // Try to parse it as single record response
            try {
                incidents.add(om.readValue(response, IncidentSingleResponse.class).getRecord());
                return incidents;
            } catch (IOException e1) {
                fail("Unable to unmarshall incident response", e);
            }
        }
        return null;
    }

    private static Incident getIncidentFromResponse(HTTPResponse r) {
        String responseBody = r.getBody();
        log.debug("Response body: " + responseBody);
        List<Incident> incidents = parseResponse(responseBody);
        assertThat(incidents).hasSize(1);
        return incidents.get(0);
    }

    public static String getIncidentJson(Incident i) {
        try {
            return om.writeValueAsString(i);
        } catch (JsonProcessingException e) {
            fail("Unable to convert incident to json", e);
        }
        return null;
    }
}
