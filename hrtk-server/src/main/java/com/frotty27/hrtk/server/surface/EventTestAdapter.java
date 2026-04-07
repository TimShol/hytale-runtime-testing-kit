package com.frotty27.hrtk.server.surface;

import com.frotty27.hrtk.server.context.LiveEventCapture;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.IBaseEvent;

public final class EventTestAdapter {

    private final EventRegistry eventRegistry;

    public EventTestAdapter(EventRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
    }

    @SuppressWarnings("unchecked")
    public <E extends IBaseEvent<Void>> LiveEventCapture<E> capture(Class<E> eventType) {
        LiveEventCapture<E> capture = new LiveEventCapture<>(eventType);
        eventRegistry.register((Class<? super E>) eventType, capture::capture);
        return capture;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E extends IBaseEvent<?>> LiveEventCapture<E> captureGlobal(Class<E> eventType) {
        LiveEventCapture<E> capture = new LiveEventCapture<>(eventType);
        eventRegistry.registerGlobal((Class) eventType, (java.util.function.Consumer) event -> capture.capture((E) event));
        return capture;
    }

    public EventRegistry getEventRegistry() { return eventRegistry; }
}
