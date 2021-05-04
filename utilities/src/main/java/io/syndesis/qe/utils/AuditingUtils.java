package io.syndesis.qe.utils;

import io.syndesis.qe.test.InfraFail;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Lazy
public class AuditingUtils {

    private static final String POD_AUDITING_LOG = "syndesis-server";
    private String lastStoredMessageId = null;

    public JSONObject getLastAuditMessage() {
        List<String> allAuditMessages = extractAuditMessages(OpenShiftUtils.getPodLogs(POD_AUDITING_LOG, 7000)); //7000 lines is enough
        if (allAuditMessages.isEmpty()) {
            InfraFail.fail(
                "The last 7000 characters from the syndesis server log don't contain any event record. Either the auditing feature is disabled or " +
                    "some error was throwed after the audit record. Check the server log.");
        }
        JSONObject result = new JSONObject(allAuditMessages.get(allAuditMessages.size() - 1));
        lastStoredMessageId = result.getString("id");
        return result;
    }

    public JSONArray getEventsFromTheLastAuditMessage() {
        JSONObject auditMessage = getLastAuditMessage();
        return auditMessage.getJSONArray("events");
    }

    private List<String> extractAuditMessages(String log) {
        List<String> allAuditMessages = new ArrayList<String>();
        Matcher m = Pattern.compile(".*AUDIT\\s*:\\s*(\\{.*})\n")
            .matcher(log);
        while (m.find()) {
            allAuditMessages.add(m.group(1)); // only AUDIT message, group 1 in regex
        }
        return allAuditMessages;
    }

    public String getLastStoredMessageId() {
        return lastStoredMessageId;
    }
}
