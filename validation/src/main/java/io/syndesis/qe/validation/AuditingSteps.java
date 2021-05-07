package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.utils.AuditingUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;

public class AuditingSteps {

    @Autowired
    private AuditingUtils auditingUtils;

    /**
     * Support keys:
     * | type | <value> |
     * | name | <value> |
     * | timestamp | <value> |
     * | user | <value> |
     * | recordType | <value> |
     */
    @Then("^check that the last audit record contains following parameters$")
    public void checkAuditMessageParameters(DataTable messageInfo) {
        JSONObject lastAuditMessage = auditingUtils.getLastAuditMessage();
        for (Map.Entry<Object, Object> entry : messageInfo.asMap(String.class, String.class).entrySet()) {
            assertThat(lastAuditMessage.has((String) entry.getKey())).isTrue();
            assertThat(lastAuditMessage.get((String) entry.getKey())).isEqualTo(entry.getValue());
        }
    }

    /**
     * Add all expected key:values as DataTable, e.g.:
     * Then check that the last audit record contains following parameters
     * | type       | connection |
     * | name       | AMQ        |
     * | user       | developer  |
     * | recordType | updated    |
     */
    @Then("^check that the last audit record contains the event with the following parameters$")
    public void checkAuditEventParameters(DataTable event) {
        JSONObject expectedEvent = new JSONObject(event.asMap(String.class, String.class));
        JSONArray lastAuditEvents = auditingUtils.getEventsFromTheLastAuditMessage();
        //unfortunately, JSONObject doesn't have implemented `equals` method. As workaround, the fieldByFieldElementCopmarator is used
        assertThat(lastAuditEvents).usingFieldByFieldElementComparator().contains(expectedEvent);
    }

    @Then("^check that the last audit record ID is the same as the previous stored one$")
    public void checkAuditIDs() {
        String previousID = auditingUtils.getLastStoredMessageId();
        JSONObject lastAuditMessage = auditingUtils.getLastAuditMessage();
        assertThat(lastAuditMessage.get("id")).isEqualTo(previousID);
    }
}
