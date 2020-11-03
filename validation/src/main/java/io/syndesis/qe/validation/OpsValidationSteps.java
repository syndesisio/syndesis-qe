package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.endpoint.ConnectionsEndpoint;
import io.syndesis.qe.endpoint.Constants;
import io.syndesis.qe.endpoint.client.EndpointClient;
import io.syndesis.qe.endpoints.PrometheusQueryEndpoint;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.assertj.core.api.SoftAssertions;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
import com.google.common.collect.Streams;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpsValidationSteps {

    @Autowired
    private PrometheusQueryEndpoint queryEndpoint;

    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;

    @Then("^verify Prometheus query \"([^\"]*)\" (decreases|increases) in (\\d+) seconds$")
    public void queryValueChanging(String query, String type, Integer timeout) {
        JsonNode res = queryEndpoint.executeQueryOnRange(query, timeout, 2);
        assertThat(res.get("status").asText()).isEqualToIgnoringCase("success");
        Iterator<JsonNode> it = res.get("data").get("result").get(0).get("values").elements();
        List<Integer> vals = Lists.newArrayList(it).stream().map(node -> node.get(1).asInt()).collect(Collectors.toList());
        if ("decreases".equalsIgnoreCase(type)) {
            assertThat(vals).isSortedAccordingTo(Comparator.reverseOrder());
        } else {
            assertThat(vals).isSortedAccordingTo(Comparator.naturalOrder());
        }
    }

    /**
     * Verifies alerts are registered in prometheus
     *
     * @param table has following structure:
     * | group_name | alert_name | query | severity | message |
     * This information can be found by executing GET to <prometheus-route>/api/v1/rules?type=alert
     */
    @Then("verify Prometheus has registered alerts")
    public void verifyAlerts(DataTable table) {
        JsonNode res = queryEndpoint.getAlertRules();
        assertThat(res.get("status").asText()).isEqualToIgnoringCase("success");
        JsonNode groups = res.get("data").get("groups");
        Map<String, Map<String, JsonNode>> groupMap = new HashMap<>();
        groups.forEach(group -> {
            groupMap.computeIfAbsent(group.get("name").asText(), k -> new HashMap<>());
            group.get("rules").forEach(rule -> {
                groupMap.get(group.get("name").asText()).put(rule.get("name").asText(), rule);
            });
        });
        SoftAssertions sa = new SoftAssertions();
        log.info("groups: {}", groupMap);
        for (List<String> row : table.asLists()) {
            log.info("table row: {}", row);
            sa.assertThat(groupMap).containsKey(row.get(0));
            sa.assertThat(groupMap.get(row.get(0))).containsKey(row.get(1));
            JsonNode rule = groupMap.get(row.get(0)).get(row.get(1));
            sa.assertThat(rule.get("query").asText()).isEqualTo(row.get(2).replace("\\\"", "\""));
            sa.assertThat(rule.get("labels").get("severity").asText()).isEqualTo(row.get(3));
            sa.assertThat(rule.get("annotations").get("message").asText()).isEqualTo(row.get(4));
        }
        sa.assertAll();
    }

    private static CustomResourceDefinitionContext dashboardContext() {
        CustomResourceDefinition crd = OpenShiftUtils.getInstance().customResourceDefinitions().withName("grafanadashboards.integreatly.org").get();
        return new CustomResourceDefinitionContext.Builder()
            .withGroup(crd.getSpec().getGroup())
            .withPlural(crd.getSpec().getNames().getPlural())
            .withScope(crd.getSpec().getScope())
            .withVersion(crd.getSpec().getVersion())
            .build();
    }

    private static CustomResourceDefinitionContext grafanaDSContext() {
        CustomResourceDefinition crd = OpenShiftUtils.getInstance().customResourceDefinitions().withName("grafanadatasources.integreatly.org").get();
        return new CustomResourceDefinitionContext.Builder()
            .withGroup(crd.getSpec().getGroup())
            .withPlural(crd.getSpec().getNames().getPlural())
            .withScope(crd.getSpec().getScope())
            .withVersion(crd.getSpec().getVersion())
            .build();
    }

    @Then("verify Grafana has correct datasource and dashboards")
    public void verifyGrafana(DataTable dashboards) {
        SoftAssertions sa = new SoftAssertions();
        Map<String, Object> prometheusDS =
            OpenShiftUtils.getInstance().customResource(grafanaDSContext()).get("application-monitoring", "prometheus");
        sa.assertThat(((Map<String, Object>) prometheusDS.get("status")).get("message")).isEqualTo("success");
        Map<String, Object> spec = (Map<String, Object>) prometheusDS.get("spec");
        List<Map<String, Object>> datasources = (List<Map<String, Object>>) spec.get("datasources");
        sa.assertThat(datasources).size().isNotZero();
        sa.assertThat(datasources).anyMatch(map -> "prometheus".equals(map.get("type")));
        sa.assertAll();

        Map<String, Object> list = OpenShiftUtils.getInstance().customResource(dashboardContext()).list(TestConfiguration.openShiftNamespace());
        List<Map<String, Object>> items = (List<Map<String, Object>>) list.get("items");
        dashboards.asList().forEach(it -> {
            sa.assertThat(items).anyMatch(map -> it.equals(((Map<String, Object>) map.get("metadata")).get("name")));
        });
        sa.assertAll();
    }

    /**
     * @param panels | dashboard-name | title | target-expr | target-type |
     */
    @Then("verify Grafana dashboards contain panels")
    public void verifyPanels(DataTable panels) throws IOException {
        SoftAssertions sa = new SoftAssertions();
        for (List<String> row : panels.asLists()) {
            Map<String, Object> res =
                OpenShiftUtils.getInstance().customResource(dashboardContext()).get(TestConfiguration.openShiftNamespace(), row.get(0));
            JsonNode dashboard = new ObjectMapper().readTree((String) ((Map<String, Object>) res.get("spec")).get("json"));
            JsonNode panelsList = dashboard.get("panels");
            Optional<JsonNode> optPanel =
                panelsList.findParents("title").stream().filter(it -> row.get(1).equals(it.get("title").asText())).findFirst();
            sa.assertThat(optPanel).isPresent();
            optPanel.ifPresent(node -> {
                sa.assertThat(node.get("targets").findValues("expr").stream().map(JsonNode::asText)).contains(row.get(2).replace("\\\"", "\""));
                sa.assertThat(node.get("type").asText()).isEqualTo(row.get(3));
            });
        }
        sa.assertAll();
    }

    private void verifyAlertIsRaised(String name) {
        JsonNode res = queryEndpoint.getAlerts();
        assertThat(res.get("status").asText()).isEqualToIgnoringCase("success");
        JsonNode alerts = res.get("data").get("alerts");
        assertThat(Streams.stream(alerts.elements())).anyMatch(alert ->
            name.equals(alert.get("labels").get("alertname").asText()) &&
                alert.get("state").asText().matches("pending|firing"));
    }

    private boolean wasAlertRaised(String alertName) {
        return Streams.stream(queryEndpoint.getAlerts().get("data").get("alerts").elements())
            .map(alert -> alert.get("labels").get("alertname").asText()).distinct().filter(alertName::equals).count() > 0;
    }

    @Then("verify monitoring alerts are working correctly")
    public void verifyAlerts() throws TimeoutException, InterruptedException {
        try {
            OpenShiftUtils.scale("syndesis-operator", 0);
            OpenShiftUtils.scale("syndesis-db", 0);
            OpenShiftWaitUtils.waitFor(() -> wasAlertRaised("FuseOnlineDatabaseInstanceDown"), 10 * 1000 * 60);
            verifyAlertIsRaised("FuseOnlineDatabaseInstanceDown");
            verifyAlertIsRaised("FuseOnlinePostgresExporterDown");
            log.info("Database tests passed\n Executing REST requests and waiting for endpoint alerts");
            for (int i = 0; i < 1000; i++) {
                if (wasAlertRaised("FuseOnlineRestApiHighEndpointErrorRate")) {
                    break;
                }
                EndpointClient.getClient()
                    .property("disable-logging", true)
                    .target(Constants.LOCAL_REST_URL).path("api").path("v1").path("connections")
                    .request()
                    .header("X-Forwarded-User", "pista")
                    .header("X-Forwarded-Access-Token", "kral")
                    .header("SYNDESIS-XSRF-TOKEN", "awesome")
                    .get();
            }
            verifyAlertIsRaised("FuseOnlineRestApiHighEndpointErrorRate");
        } finally {
            OpenShiftUtils.scale("syndesis-operator", 1);
            OpenShiftUtils.scale("syndesis-db", 1);
        }
    }
}
