package com.frotty27.hrtk.api.mock;

import java.util.List;
import java.util.function.Predicate;

/**
 * Captures events of a specific type for later assertion in tests.
 *
 * <p>Created via {@link com.frotty27.hrtk.api.context.TestContext#captureEvent(Class)}.
 * The capture begins recording immediately upon creation. Call {@link #close()} to
 * stop capturing early, or let the framework clean it up at the end of the test.</p>
 *
 * <pre>{@code
 * EventCapture<PlayerJoinEvent> capture = ctx.captureEvent(PlayerJoinEvent.class);
 * // ... trigger the event ...
 * assertTrue(capture.wasFired());
 * assertEquals(1, capture.getCount());
 * PlayerJoinEvent event = capture.getFirst();
 * }</pre>
 *
 * @param <E> the event type being captured
 * @see com.frotty27.hrtk.api.context.TestContext#captureEvent(Class)
 * @see com.frotty27.hrtk.api.assert_.EventAssert
 * @since 1.0.0
 */
public interface EventCapture<E> {

    /**
     * Returns all captured events in the order they were fired.
     *
     * @return an unmodifiable list of captured events
     */
    List<E> getEvents();

    /**
     * Returns the number of events captured so far.
     *
     * @return the event count
     */
    int getCount();

    /**
     * Checks whether at least one event has been captured.
     *
     * @return {@code true} if one or more events were captured
     */
    boolean wasFired();

    /**
     * Checks whether any captured event matches the given predicate.
     *
     * @param predicate a predicate to test each captured event against
     * @return {@code true} if at least one event matches
     */
    boolean anyMatch(Predicate<E> predicate);

    /**
     * Returns the first captured event.
     *
     * @return the first event, or {@code null} if none were captured
     */
    E getFirst();

    /**
     * Returns the last (most recent) captured event.
     *
     * @return the last event, or {@code null} if none were captured
     */
    E getLast();

    /**
     * Clears all captured events, resetting the count to zero.
     */
    void clear();

    /**
     * Stops capturing events by unregistering the underlying listener.
     *
     * <p>After calling this method, no further events will be recorded. The
     * already-captured events remain accessible.</p>
     */
    void close();
}
