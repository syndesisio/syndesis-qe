package io.syndesis.qe.utils;

/**
 * Abstract base for syndesis rest tests. These connections ids/constants are set by fuseqe to identyfy easily specific
 * connector.
 *
 * Jun 26, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
public final class RestConstants {

    public static final String SALESFORCE_CONNECTION_ID = "fuseqe-salesforce";
    public static final String TWITTER_CONNECTION_ID = "fuseqe-twitter";
    public static final String FTP_CONNECTION_ID = "fuseqe-ftp";
    public static final String DROPBOX_CONNECTION_ID = "fuseqe-dropbox";
    public static final String SYNDESIS_TALKY_ACCOUNT = "twitter_talky";
    public static final String TODO_APP_NAME = "todo";
    public static final String AMQ_CONNECTION_ID = "fuseqe-amq";
    public static final String KAFKA_CONNECTION_ID = "fuseqe-kafka";
}
