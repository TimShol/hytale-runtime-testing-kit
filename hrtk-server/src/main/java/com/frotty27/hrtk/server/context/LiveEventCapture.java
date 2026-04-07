package com.frotty27.hrtk.server.context;

import com.frotty27.hrtk.api.mock.EventCapture;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class LiveEventCapture<E> implements EventCapture<E> {

    private final Class<E> eventType;
    private final List<E> events = new CopyOnWriteArrayList<>();
    private volatile boolean closed;
    private Object registration;

    public LiveEventCapture(Class<E> eventType) {
        this.eventType = eventType;
    }

    public void capture(E event) {
        if (!closed) {
            events.add(event);
        }
    }

    public Class<E> getEventType() {
        return eventType;
    }

    @Override
    public List<E> getEvents() {
        return Collections.unmodifiableList(events);
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public boolean wasFired() {
        return !events.isEmpty();
    }

    @Override
    public boolean anyMatch(Predicate<E> predicate) {
        for (E event : events) {
            if (predicate.test(event)) return true;
        }
        return false;
    }

    @Override
    public E getFirst() {
        return events.isEmpty() ? null : events.getFirst();
    }

    @Override
    public E getLast() {
        return events.isEmpty() ? null : events.getLast();
    }

    @Override
    public void clear() {
        events.clear();
    }

    public void setRegistration(Object registration) {
        this.registration = registration;
    }

    @Override
    public void close() {
        closed = true;
        if (registration != null) {
            try {
                var unregisterMethod = registration.getClass().getMethod("unregister");
                unregisterMethod.invoke(registration);
            } catch (Exception _) {}
            registration = null;
        }
    }
}
