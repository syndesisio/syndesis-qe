package io.syndesis.qe.utils;

import lombok.Getter;

/**
 * Abstract base for syndesis rest tests.
 * These connections ids/constants are set by fuseqe to identyfy easily specific connector.
 *
 * Jun 26, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
public final class RestConstants {

    private static RestConstants instance = null;
    @Getter
    private static final String SALESFORCE_CONNECTION_ID = "fuseqe-salesforce";
    @Getter
    private static final String TWITTER_CONNECTION_ID = "fuseqe-twitter";
    @Getter
    private static final String FTP_CONNECTION_ID = "fuseqe-ftp";
    @Getter
    private static final String SYNDESIS_TALKY_ACCOUNT = "twitter_talky";
    @Getter
    private static final String TODO_APP_NAME = "todo";

    public static RestConstants getInstance() {
        if (instance == null) {
            instance = new RestConstants();
        }
        return instance;
    }

    private RestConstants() {
    }
}
