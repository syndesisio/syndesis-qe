package io.syndesis.qe.rest.exceptions;

/**
 * @author jknetl
 */
public class RestClientException extends RuntimeException {

	public RestClientException() {
	}

	public RestClientException(String message) {
		super(message);
	}

	public RestClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public RestClientException(Throwable cause) {
		super(cause);
	}

	public RestClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
