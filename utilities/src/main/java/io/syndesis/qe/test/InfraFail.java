package io.syndesis.qe.test;

import io.syndesis.qe.utils.TestUtils;

import org.assertj.core.api.Assertions;

public class InfraFail {
    /**
     * Throws an {@link AssertionError} with the given message.
     *
     * @param failureMessage error message.
     * @throws AssertionError with the given message.
     */
    public static void fail(String failureMessage) {
        TestUtils.saveDebugInfo();
        Assertions.fail(failureMessage);
    }

    /**
     * Throws an {@link AssertionError} with the given message built as {@link String#format(String, Object...)}.
     *
     * @param failureMessage error message.
     * @param args Arguments referenced by the format specifiers in the format string.
     * @throws AssertionError with the given built message.
     */
    public static void fail(String failureMessage, Object... args) {
        TestUtils.saveDebugInfo();
        Assertions.fail(failureMessage, args);
    }

    /**
     * Throws an {@link AssertionError} with the given message and with the {@link Throwable} that caused the failure.
     *
     * @param failureMessage the description of the failed assertion. It can be {@code null}.
     * @param realCause cause of the error.
     * @throws AssertionError with the given message and with the {@link Throwable} that caused the failure.
     */
    public static void fail(String failureMessage, Throwable realCause) {
        TestUtils.saveDebugInfo();
        Assertions.fail(failureMessage, realCause);
    }
}
