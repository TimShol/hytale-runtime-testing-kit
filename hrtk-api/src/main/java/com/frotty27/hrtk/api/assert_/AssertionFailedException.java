package com.frotty27.hrtk.api.assert_;

/**
 * Thrown when a test assertion fails within the HRTK framework.
 *
 * <p>Extends {@link RuntimeException} so test methods do not need to declare a
 * throws clause. The HRTK test runner catches all {@link Throwable}s during
 * execution, so assertion failures never crash the server.</p>
 *
 * <pre>{@code
 * throw new AssertionFailedException("Expected 5 but got 3");
 * throw new AssertionFailedException("Decode failed", cause);
 * }</pre>
 *
 * @see HytaleAssert#fail(String)
 * @since 1.0.0
 */
public class AssertionFailedException extends RuntimeException {

    /**
     * Creates a new assertion failure with the given message.
     *
     * @param message a description of the assertion that failed
     */
    public AssertionFailedException(String message) {
        super(message);
    }

    /**
     * Creates a new assertion failure with the given message and underlying cause.
     *
     * @param message a description of the assertion that failed
     * @param cause   the underlying exception that triggered this failure
     */
    public AssertionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
