package io.syndesis.qe.rest.tests.util;

import lombok.Getter;
import lombok.Setter;

public class RestTestsUtils {
    public enum Connector {
        ACTIVEMQ("activemq"),
        DB("5"),
        DROPBOX("dropbox"),
        FTP("ftp"),
        HTTP("http4"),
        HTTPS("https4"),
        KAFKA("kafka"),
        SALESFORCE("salesforce"),
        S3("aws-s3"),
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
        DB("sql"),
        DROPBOX("fuseqe-dropbox"),
        FTP("fuseqe-ftp"),
        HTTP("fuseqe-http"),
        HTTPS("fuseqe-https"),
        KAFKA("fuseqe-kafka"),
        SALESFORCE("fuseqe-salesforce"),
        TWITTER("fuseqe-twitter");

        @Setter
        @Getter
        private String id;

        Connection(String id) {
            this.id = id;
        }
    }
}
