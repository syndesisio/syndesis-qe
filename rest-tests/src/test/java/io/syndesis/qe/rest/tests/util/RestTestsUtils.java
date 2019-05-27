package io.syndesis.qe.rest.tests.util;

import lombok.Getter;
import lombok.Setter;

public class RestTestsUtils {
    public enum Connector {
        ACTIVEMQ("activemq"),
        BOX("box"),
        DB("sql"),
        DROPBOX("dropbox"),
        FTP("ftp"),
        HTTP("http4"),
        HTTPS("https4"),
        IRC("irc"),
        KAFKA("kafka"),
        SALESFORCE("salesforce"),
        S3("aws-s3"),
        SNS("aws-sns"),
        SQS("aws-sqs"),
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
        BOX("fuseqe-box"),
        DB("5"),
        DROPBOX("fuseqe-dropbox"),
        FTP("fuseqe-ftp"),
        HTTP("fuseqe-http"),
        HTTPS("fuseqe-https"),
        IRC("irc"),
        KAFKA("fuseqe-kafka"),
        SALESFORCE("fuseqe-salesforce"),
        SNS("fuseqe-sns"),
        SQS("fuseqe-sqs"),
        TWITTER("fuseqe-twitter");

        @Setter
        @Getter
        private String id;

        Connection(String id) {
            this.id = id;
        }
    }
}
