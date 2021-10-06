package io.syndesis.qe.account;

import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains information about a third party service account.
 *
 * @author jknetl
 */
@Data
public class Account {
    public enum Name {
        ACTIVEMQ("AMQ"),
        AMQP("AMQP"),
        AWS("AWS"),
        AWS_DDB("AWS DDB"),
        BOX("Box"),
        CONCUR("QE Concur"),
        DROPBOX("QE Dropbox"),
        FTP("ftp"),
        FHIR("fhir"),
        SFTP("sftp"),
        GITHUB("GitHub"),
        HTTP("http"),
        HTTPS("https"),
        IRC("irc"),
        JIRA_HOOK("Jira Hook"),
        KAFKA("kafka"),
        MANAGED_KAFKA("Managed Kafka"),
        KUDU("kudu"),
        MONGODB36("mongodb36"),
        MQTT("QE MQTT"),
        ODATA_HTTP("odata V4"),
        ODATA_HTTPS("odataHttps"),
        ODATA_V2("odata V2"),
        SALESFORCE("QE Salesforce"),
        SERVICENOW("Servicenow"),
        SLACK("QE Slack"),
        SYNDESIS_DB("SyndesisDB"),
        TELEGRAM("telegram"),
        TWITTER_LISTENER("Twitter Listener"),
        TWITTER_TALKY("twitter_talky"),
        ZENHUB("ZenHub");

        @Setter
        @Getter
        private String id;

        Name(String id) {
            this.id = id;
        }
    }

    @Getter
    private String service;
    private Map<String, String> properties;

    public String getProperty(String name) {
        return properties.get(name);
    }
}
