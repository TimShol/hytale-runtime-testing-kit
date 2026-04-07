package com.frotty27.hrtk.api.assert_;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * Core assertion methods for all HRTK test types.
 *
 * <p>Provides general-purpose assertions for equality, nullity, boolean conditions,
 * exception checking, timeouts, collections, strings, and numeric comparisons.
 * All assertion methods throw {@link AssertionFailedException} on failure.</p>
 *
 * <pre>{@code
 * HytaleAssert.assertEquals("stone", blockId);
 * HytaleAssert.assertTrue(player.isOnline());
 * HytaleAssert.assertThrows(IllegalArgumentException.class, () -> parse("bad"));
 * }</pre>
 *
 * @see AssertionFailedException
 * @since 1.0.0
 */
public final class HytaleAssert {

    private HytaleAssert() {}

    /**
     * Asserts that two objects are equal using {@link Objects#equals(Object, Object)}.
     *
     * <p>Failure message: {@code "Expected <expected> but was <actual>"}</p>
     *
     * @param expected the expected value
     * @param actual   the actual value to check
     * @throws AssertionFailedException if the values are not equal
     */
    public static void assertEquals(Object expected, Object actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Asserts that two objects are equal using {@link Objects#equals(Object, Object)},
     * with a custom message prefix.
     *
     * <p>Failure message: {@code "message: Expected <expected> but was <actual>"}</p>
     *
     * @param message  a custom message prefix, or {@code null} for the default
     * @param expected the expected value
     * @param actual   the actual value to check
     * @throws AssertionFailedException if the values are not equal
     */
    public static void assertEquals(String message, Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            fail(formatMessage(message, "Expected <%s> but was <%s>", expected, actual));
        }
    }

    /**
     * Asserts that two objects are not equal using {@link Objects#equals(Object, Object)}.
     *
     * <p>Failure message: {@code "Expected value to differ from <unexpected> but was equal"}</p>
     *
     * @param unexpected the value that {@code actual} should differ from
     * @param actual     the actual value to check
     * @throws AssertionFailedException if the values are equal
     */
    public static void assertNotEquals(Object unexpected, Object actual) {
        if (Objects.equals(unexpected, actual)) {
            fail("Expected value to differ from <%s> but was equal", unexpected);
        }
    }

    /**
     * Asserts that a condition is {@code true}.
     *
     * <p>Failure message: {@code "Expected true but was false"}</p>
     *
     * @param condition the boolean condition to check
     * @throws AssertionFailedException if the condition is {@code false}
     */
    public static void assertTrue(boolean condition) {
        assertTrue(null, condition);
    }

    /**
     * Asserts that a condition is {@code true}, with a custom message prefix.
     *
     * <p>Failure message: {@code "message: Expected true but was false"}</p>
     *
     * @param message   a custom message prefix, or {@code null} for the default
     * @param condition the boolean condition to check
     * @throws AssertionFailedException if the condition is {@code false}
     */
    public static void assertTrue(String message, boolean condition) {
        if (!condition) {
            fail(formatMessage(message, "Expected true but was false"));
        }
    }

    /**
     * Asserts that a condition is {@code false}.
     *
     * <p>Failure message: {@code "Expected false but was true"}</p>
     *
     * @param condition the boolean condition to check
     * @throws AssertionFailedException if the condition is {@code true}
     */
    public static void assertFalse(boolean condition) {
        assertFalse(null, condition);
    }

    /**
     * Asserts that a condition is {@code false}, with a custom message prefix.
     *
     * <p>Failure message: {@code "message: Expected false but was true"}</p>
     *
     * @param message   a custom message prefix, or {@code null} for the default
     * @param condition the boolean condition to check
     * @throws AssertionFailedException if the condition is {@code true}
     */
    public static void assertFalse(String message, boolean condition) {
        if (condition) {
            fail(formatMessage(message, "Expected false but was true"));
        }
    }

    /**
     * Asserts that an object is {@code null}.
     *
     * <p>Failure message: {@code "Expected null but was <object>"}</p>
     *
     * @param object the object to check
     * @throws AssertionFailedException if the object is not {@code null}
     */
    public static void assertNull(Object object) {
        assertNull(null, object);
    }

    /**
     * Asserts that an object is {@code null}, with a custom message prefix.
     *
     * <p>Failure message: {@code "message: Expected null but was <object>"}</p>
     *
     * @param message a custom message prefix, or {@code null} for the default
     * @param object  the object to check
     * @throws AssertionFailedException if the object is not {@code null}
     */
    public static void assertNull(String message, Object object) {
        if (object != null) {
            fail(formatMessage(message, "Expected null but was <%s>", object));
        }
    }

    /**
     * Asserts that an object is not {@code null}.
     *
     * <p>Failure message: {@code "Expected non-null value but was null"}</p>
     *
     * @param object the object to check
     * @throws AssertionFailedException if the object is {@code null}
     */
    public static void assertNotNull(Object object) {
        assertNotNull(null, object);
    }

    /**
     * Asserts that an object is not {@code null}, with a custom message prefix.
     *
     * <p>Failure message: {@code "message: Expected non-null value but was null"}</p>
     *
     * @param message a custom message prefix, or {@code null} for the default
     * @param object  the object to check
     * @throws AssertionFailedException if the object is {@code null}
     */
    public static void assertNotNull(String message, Object object) {
        if (object == null) {
            fail(formatMessage(message, "Expected non-null value but was null"));
        }
    }

    /**
     * Asserts that the given runnable throws an exception of the expected type.
     *
     * <p>Failure message: {@code "Expected <type> but got <actual>: message"} or
     * {@code "Expected <type> to be thrown but nothing was thrown"}</p>
     *
     * @param expectedType the expected exception class
     * @param runnable     the code to execute
     * @param <T>          the expected exception type
     * @return the caught exception instance
     * @throws AssertionFailedException if no exception is thrown or the wrong type is thrown
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T assertThrows(Class<T> expectedType, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return (T) t;
            }
            fail("Expected <%s> but got <%s>: %s", expectedType.getSimpleName(),
                    t.getClass().getSimpleName(), t.getMessage());
        }
        fail("Expected <%s> to be thrown but nothing was thrown", expectedType.getSimpleName());
        return null;
    }

    /**
     * Asserts that the given runnable does not throw any exception.
     *
     * <p>Failure message: {@code "Expected no exception but got <type>: message"}</p>
     *
     * @param runnable the code to execute
     * @throws AssertionFailedException if any exception is thrown
     */
    public static void assertDoesNotThrow(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            fail("Expected no exception but got <%s>: %s", t.getClass().getSimpleName(), t.getMessage());
        }
    }

    /**
     * Asserts that the given runnable completes within the specified timeout.
     *
     * <p>The runnable is executed asynchronously. If it does not complete before
     * the timeout elapses, the assertion fails.</p>
     *
     * <p>Failure message: {@code "Execution timed out after <ms>ms"}</p>
     *
     * @param timeout  the maximum duration to wait
     * @param runnable the code to execute
     * @throws AssertionFailedException if execution times out or throws an exception
     */
    public static void assertTimeout(Duration timeout, Runnable runnable) {
        var future = CompletableFuture.runAsync(runnable);
        try {
            future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            fail("Execution timed out after %dms", timeout.toMillis());
        } catch (Exception e) {
            fail("Execution failed: %s", e.getMessage());
        }
    }

    /**
     * Asserts that two arrays are deeply equal using {@link Arrays#deepEquals(Object[], Object[])}.
     *
     * <p>Failure message: {@code "Expected array [...] but was [...]"}</p>
     *
     * @param expected the expected array
     * @param actual   the actual array to check
     * @throws AssertionFailedException if the arrays are not deeply equal
     */
    public static void assertArrayEquals(Object[] expected, Object[] actual) {
        if (!Arrays.deepEquals(expected, actual)) {
            fail("Expected array %s but was %s", Arrays.deepToString(expected), Arrays.deepToString(actual));
        }
    }

    /**
     * Asserts that a collection contains the given element.
     *
     * <p>Failure message: {@code "Expected collection to contain <element> but it did not"}</p>
     *
     * @param collection the collection to search
     * @param element    the element to look for
     * @param <T>        the element type
     * @throws AssertionFailedException if the collection does not contain the element
     */
    public static <T> void assertContains(Collection<T> collection, T element) {
        assertNotNull("Collection", collection);
        if (!collection.contains(element)) {
            fail("Expected collection to contain <%s> but it did not", element);
        }
    }

    /**
     * Asserts that a collection does not contain the given element.
     *
     * <p>Failure message: {@code "Expected collection to not contain <element> but it did"}</p>
     *
     * @param collection the collection to search
     * @param element    the element that should be absent
     * @param <T>        the element type
     * @throws AssertionFailedException if the collection contains the element
     */
    public static <T> void assertNotContains(Collection<T> collection, T element) {
        assertNotNull("Collection", collection);
        if (collection.contains(element)) {
            fail("Expected collection to not contain <%s> but it did", element);
        }
    }

    /**
     * Asserts that a collection is empty.
     *
     * <p>Failure message: {@code "Expected empty collection but had N elements"}</p>
     *
     * @param collection the collection to check
     * @throws AssertionFailedException if the collection is not empty
     */
    public static void assertEmpty(Collection<?> collection) {
        assertNotNull("Collection", collection);
        if (!collection.isEmpty()) {
            fail("Expected empty collection but had %d elements", collection.size());
        }
    }

    /**
     * Asserts that a collection is not empty.
     *
     * <p>Failure message: {@code "Expected non-empty collection but was empty"}</p>
     *
     * @param collection the collection to check
     * @throws AssertionFailedException if the collection is empty
     */
    public static void assertNotEmpty(Collection<?> collection) {
        assertNotNull("Collection", collection);
        if (collection.isEmpty()) {
            fail("Expected non-empty collection but was empty");
        }
    }

    /**
     * Asserts that a string matches the given regular expression.
     *
     * <p>Failure message: {@code "Expected <actual> to match regex <regex>"}</p>
     *
     * @param regex  the regular expression pattern
     * @param actual the string to test
     * @throws AssertionFailedException if the string does not match the pattern
     */
    public static void assertMatches(String regex, String actual) {
        assertNotNull("Actual string", actual);
        if (!Pattern.matches(regex, actual)) {
            fail("Expected <%s> to match regex <%s>", actual, regex);
        }
    }

    /**
     * Asserts that a string contains the expected substring.
     *
     * <p>Failure message: {@code "Expected <actual> to contain <expected>"}</p>
     *
     * @param expected the substring to look for
     * @param actual   the string to search within
     * @throws AssertionFailedException if the actual string does not contain the expected substring
     */
    public static void assertContainsString(String expected, String actual) {
        assertNotNull("Actual string", actual);
        if (!actual.contains(expected)) {
            fail("Expected <%s> to contain <%s>", actual, expected);
        }
    }

    /**
     * Asserts that two doubles are equal within a tolerance (delta).
     *
     * <p>Failure message: {@code "Expected <expected> (within delta <delta>) but was <actual>"}</p>
     *
     * @param expected the expected value
     * @param actual   the actual value to check
     * @param delta    the maximum acceptable difference
     * @throws AssertionFailedException if the difference exceeds the delta
     */
    public static void assertEquals(double expected, double actual, double delta) {
        if (Math.abs(expected - actual) > delta) {
            fail("Expected <%s> (within delta %s) but was <%s>", expected, delta, actual);
        }
    }

    /**
     * Asserts that the actual value is strictly greater than the expected threshold.
     *
     * <p>Failure message: {@code "Expected value greater than <expected> but was <actual>"}</p>
     *
     * @param expected the threshold (exclusive lower bound)
     * @param actual   the actual value to check
     * @throws AssertionFailedException if actual is less than or equal to expected
     */
    public static void assertGreaterThan(long expected, long actual) {
        if (actual <= expected) {
            fail("Expected value greater than <%d> but was <%d>", expected, actual);
        }
    }

    /**
     * Asserts that the actual value is strictly less than the expected threshold.
     *
     * <p>Failure message: {@code "Expected value less than <expected> but was <actual>"}</p>
     *
     * @param expected the threshold (exclusive upper bound)
     * @param actual   the actual value to check
     * @throws AssertionFailedException if actual is greater than or equal to expected
     */
    public static void assertLessThan(long expected, long actual) {
        if (actual >= expected) {
            fail("Expected value less than <%d> but was <%d>", expected, actual);
        }
    }

    /**
     * Asserts that two references point to the same object instance (identity check).
     *
     * <p>Failure message: {@code "Expected same instance but got different objects"}</p>
     *
     * @param expected the expected object reference
     * @param actual   the actual object reference
     * @throws AssertionFailedException if the references do not point to the same instance
     */
    public static void assertSame(Object expected, Object actual) {
        if (expected != actual) {
            fail("Expected same instance but got different objects");
        }
    }

    /**
     * Asserts that two references do not point to the same object instance.
     *
     * <p>Failure message: {@code "Expected different instances but got same object"}</p>
     *
     * @param unexpected the object reference that {@code actual} should differ from
     * @param actual     the actual object reference
     * @throws AssertionFailedException if the references point to the same instance
     */
    public static void assertNotSame(Object unexpected, Object actual) {
        if (unexpected == actual) {
            fail("Expected different instances but got same object");
        }
    }

    /**
     * Unconditionally fails the current test with the given message.
     *
     * @param message the failure message
     * @throws AssertionFailedException always
     */
    public static void fail(String message) {
        throw new AssertionFailedException(message);
    }

    /**
     * Unconditionally fails the current test with a formatted message.
     *
     * @param format the format string (as per {@link String#format(String, Object...)})
     * @param args   the format arguments
     * @throws AssertionFailedException always
     */
    public static void fail(String format, Object... args) {
        throw new AssertionFailedException(String.format(format, args));
    }

    private static String formatMessage(String userMessage, String defaultMessage, Object... args) {
        String formatted = String.format(defaultMessage, args);
        return userMessage != null ? userMessage + ": " + formatted : formatted;
    }

    private static String formatMessage(String userMessage, String defaultMessage) {
        return userMessage != null ? userMessage + ": " + defaultMessage : defaultMessage;
    }
}
