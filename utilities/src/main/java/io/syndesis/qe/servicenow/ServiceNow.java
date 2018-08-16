package io.syndesis.qe.servicenow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

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

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.servicenow.model.Incident;
import io.syndesis.qe.servicenow.model.IncidentRecordList;
import io.syndesis.qe.servicenow.model.IncidentSingleResponse;
import io.syndesis.qe.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.Response;

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
        Optional<Account> serviceNow = AccountsDirectory.getInstance().getAccount("Servicenow");
        assertThat(serviceNow).isPresent();
        String instanceName = serviceNow.get().getProperty("instanceName");
        userName = serviceNow.get().getProperty("userName");
        password = serviceNow.get().getProperty("password");
        url = String.format("https://%s.service-now.com/%s", instanceName, INCIDENT_ENDPOINT);
        om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static Incident createIncident(Incident i) {
        String json = null;
        try {
            json = om.writeValueAsString(i);
        } catch (JsonProcessingException e) {
            fail("Unable to convert incident to json", e);
        }

        log.debug(String.format("Sending incident Json: %s", json));

        Response r = HttpUtils.doPostRequest(
                url,
                json,
                "application/json",
                Headers.of("Authorization", "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes()))
        );

        assertThat(r.code()).isEqualTo(201);
        try {
            String responseBody = r.body().string();
            log.debug("Response body: " + responseBody);
            List<Incident> incidents = parseResponse(responseBody);
            assertThat(incidents).hasSize(1);
            return incidents.get(0);
        } catch (IOException e) {
            fail("Unable to unmarshall incident response", e);
        }
        return null;
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

        Response r = HttpUtils.doGetRequest(
                getUrl,
                Headers.of("Authorization", "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes()))
        );

        assertThat(r.code()).isEqualTo(200);
        try {
            String responseBody = r.body().string();
            log.debug("Response body: " + responseBody);
            return parseResponse(responseBody);
        } catch (IOException e) {
            fail("Unable to unmarshall incident response", e);
        }
        return null;
    }

    public static void deleteIncident(String sysId) {
        Response r = HttpUtils.doDeleteRequest(
                String.format("%s/%s", url, sysId),
                Headers.of("Authorization", "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes()))
        );

        assertThat(r.code()).isEqualTo(204);
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
}
