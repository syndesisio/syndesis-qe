package io.syndesis.qe.utils.http;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
/**
 * Wrapper for {@link okhttp3.Response}
 */
public class HTTPResponse {
    private String body;
    private int code;

    public HTTPResponse(String body, int code) {
        log.debug("Response code: " + code);
        log.debug("Response body: " + body);
        this.body = body;
        this.code = code;
    }
}
