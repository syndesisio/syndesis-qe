package io.syndesis.qe.rest.exceptions;

/**
 * @author jknetl
 */
public class GitHubException extends RuntimeException {
	public GitHubException() {
	}

	public GitHubException(String message) {
		super(message);
	}

	public GitHubException(String message, Throwable cause) {
		super(message, cause);
	}

	public GitHubException(Throwable cause) {
		super(cause);
	}

	public GitHubException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
