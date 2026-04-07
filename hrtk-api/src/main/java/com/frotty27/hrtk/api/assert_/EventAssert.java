package com.frotty27.hrtk.api.assert_;

import com.frotty27.hrtk.api.mock.EventCapture;

import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * Assertions for event capture and verification.
 *
 * <p>Works in conjunction with {@link EventCapture} to verify that events were
 * fired (or not fired) during test execution, and to validate event payloads
 * against predicates.</p>
 *
 * <pre>{@code
 * EventCapture<PlayerJoinEvent> capture = ctx.captureEvent(PlayerJoinEvent.class);
 * // ... trigger the event ...
 * EventAssert.assertEventFired(capture);
 * EventAssert.assertEventFiredWith(capture, e -> e.getPlayer().equals(player));
 * }</pre>
 *
 * @see EventCapture
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class EventAssert {

    private EventAssert() {}

    /**
     * Asserts that at least one event was captured.
     *
     * <p>Failure message: {@code "Expected event to be fired but it was not"}</p>
     *
     * @param capture the event capture to check
     * @param <E>     the event type
     * @throws AssertionFailedException if no events were captured
     */
    public static <E> void assertEventFired(EventCapture<E> capture) {
        if (!capture.wasFired()) {
            HytaleAssert.fail("Expected event to be fired but it was not");
        }
    }

    /**
     * Asserts that exactly the specified number of events were captured.
     *
     * <p>Failure message: {@code "Expected N events but captured M"}</p>
     *
     * @param capture       the event capture to check
     * @param expectedCount the exact number of events expected
     * @param <E>           the event type
     * @throws AssertionFailedException if the captured count does not match
     */
    public static <E> void assertEventFired(EventCapture<E> capture, int expectedCount) {
        int actual = capture.getCount();
        if (actual != expectedCount) {
            HytaleAssert.fail("Expected %d events but captured %d", expectedCount, actual);
        }
    }

    /**
     * Asserts that no events were captured.
     *
     * <p>Failure message: {@code "Expected no events but captured N"}</p>
     *
     * @param capture the event capture to check
     * @param <E>     the event type
     * @throws AssertionFailedException if any events were captured
     */
    public static <E> void assertEventNotFired(EventCapture<E> capture) {
        if (capture.wasFired()) {
            HytaleAssert.fail("Expected no events but captured %d", capture.getCount());
        }
    }

    /**
     * Asserts that at least one captured event matches the given predicate.
     *
     * <p>First verifies that at least one event was captured, then checks each
     * captured event against the matcher.</p>
     *
     * <p>Failure message: {@code "Expected at least one event matching predicate but none matched
     * (N events captured)"}</p>
     *
     * @param capture the event capture to search
     * @param matcher a predicate that returns {@code true} for a matching event
     * @param <E>     the event type
     * @throws AssertionFailedException if no events were captured or none match
     */
    public static <E> void assertEventFiredWith(EventCapture<E> capture, Predicate<E> matcher) {
        if (!capture.wasFired()) {
            HytaleAssert.fail("Expected event to be fired but it was not");
        }
        if (!capture.anyMatch(matcher)) {
            HytaleAssert.fail("Expected at least one event matching predicate but none matched (%d events captured)",
                    capture.getCount());
        }
    }

    /**
     * Asserts that the given event has been cancelled.
     *
     * <p>Checks the {@code isCancelled()} method on the event object via reflection.
     * This is intended for events implementing the {@code ICancellable} interface.</p>
     *
     * <p>Failure message: {@code "Expected event to be cancelled but it was not"}</p>
     *
     * @param event the event object (runtime type: any event implementing {@code ICancellable})
     * @throws AssertionFailedException if the event is null or not cancelled
     */
    public static void assertEventCancelled(Object event) {
        if (event == null) {
            HytaleAssert.fail("event must not be null");
        }
        boolean cancelled = invokeIsCancelled(event);
        if (!cancelled) {
            HytaleAssert.fail("Expected event to be cancelled but it was not");
        }
    }

    /**
     * Asserts that the given event has NOT been cancelled.
     *
     * <p>Checks the {@code isCancelled()} method on the event object via reflection.
     * This is intended for events implementing the {@code ICancellable} interface.</p>
     *
     * <p>Failure message: {@code "Expected event to NOT be cancelled but it was"}</p>
     *
     * @param event the event object (runtime type: any event implementing {@code ICancellable})
     * @throws AssertionFailedException if the event is null or is cancelled
     */
    public static void assertEventNotCancelled(Object event) {
        if (event == null) {
            HytaleAssert.fail("event must not be null");
        }
        boolean cancelled = invokeIsCancelled(event);
        if (cancelled) {
            HytaleAssert.fail("Expected event to NOT be cancelled but it was");
        }
    }

    private static boolean invokeIsCancelled(Object event) {
        try {
            for (Method method : event.getClass().getMethods()) {
                if ("isCancelled".equals(method.getName()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(event);
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                }
            }
        } catch (Exception e) {
            HytaleAssert.fail("Failed to invoke isCancelled() on event: %s", e.getMessage());
        }
        HytaleAssert.fail("Event %s does not have isCancelled() method - is it ICancellable?",
                event.getClass().getSimpleName());
        return false;
    }
}
