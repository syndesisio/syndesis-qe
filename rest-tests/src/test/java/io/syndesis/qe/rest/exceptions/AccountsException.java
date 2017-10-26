package io.syndesis.qe.rest.exceptions;

/**
 * Exception indicating problem with a third party service account.
 * @author jknetl
 */
public class AccountsException extends RuntimeException {
	public AccountsException() {
	}

	public AccountsException(String message) {
		super(message);
	}

	public AccountsException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccountsException(Throwable cause) {
		super(cause);
	}

	public AccountsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
