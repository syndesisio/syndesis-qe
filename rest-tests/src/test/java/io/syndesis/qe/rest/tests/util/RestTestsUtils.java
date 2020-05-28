package io.syndesis.qe.rest.tests.util;

import lombok.Getter;
import lombok.Setter;

public class RestTestsUtils {
    public enum Connector {
        ACTIVEMQ("activemq"),
        AMQP("amqp"),
        BOX("box"),
        DB("sql"),
        DROPBOX("dropbox"),
        DYNAMO_DB("aws-ddb"),
        EMAIL_RECEIVE("email-receive"),
        EMAIL_SEND("email-send"),
        FHIR("fhir"),
        FTP("ftp"),
        HTTP("http"),
        HTTPS("https"),
        IRC("irc"),
        JIRA("jira"),
        KAFKA("kafka"),
        KUDU("kudu"),
        MONGODB36("mongodb3"),
        ODATA("odata"),
        SALESFORCE("salesforce"),
        SERVICENOW("servicenow"),
        SFTP("sftp"),
        SLACK("slack"),
        SNS("aws-sns"),
        SQS("aws-sqs"),
        S3("aws-s3"),
        TELEGRAM("telegram"),
        TWITTER("twitter");

        @Setter
        @Getter
        private String id;

        Connector(String id) {
            this.id = id;
        }
    }

    public enum Connection {
        ACTIVEMQ("fuseqe-activemq"),
        AMQP("fuseqe-amqp"),
        BOX("fuseqe-box"),
        DB("5"),
        DROPBOX("fuseqe-dropbox"),
        DYNAMO_DB("fuseqe-dynamodb"),
        EMAIL_RECEIVE("fuseqe-email-receive"),
        EMAIL_SEND("fuseqe-email-send"),
        FHIR("fuseqe-fhir"),
        FTP("fuseqe-ftp"),
        SFTP("fuseqe-sftp"),
        HTTP("fuseqe-http"),
        HTTPS("fuseqe-https"),
        IRC("irc"),
        JIRA("jira"),
        KAFKA("fuseqe-kafka"),
        KUDU("fuseqe-kudu"),
        MONGODB36("fuseqe-mongo"),
        ODATA("fuseqe-odata"),
        SALESFORCE("fuseqe-salesforce"),
        SERVICENOW("fuseqe-servicenow"),
        SLACK("fuseqe-slack"),
        SNS("fuseqe-sns"),
        SQS("fuseqe-sqs"),
        TELEGRAM("fuseqe-telegram"),
        TWITTER("fuseqe-twitter");

        @Setter
        @Getter
        private String id;

        Connection(String id) {
            this.id = id;
        }
    }
}
