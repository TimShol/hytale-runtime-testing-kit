package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.annotation.WorldTest;
import com.frotty27.hrtk.api.assert_.EventAssert;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.context.TestContext;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.entity.EntityRemoveEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;

@HytaleSuite(value = "Event Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("events")
public class EventTests {

    @WorldTest
    @DisplayName("Event capture detects a fired event")
    @Order(1)
    void testEventFired(WorldTestContext ctx) {
        var capture = ctx.captureEvent(EntityRemoveEvent.class);

        var entity = ctx.spawnEntity("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();
        ctx.despawn(entity);

        EventAssert.assertEventFired(capture);
    }

    @HytaleTest
    @DisplayName("Event does not fire when conditions are not met")
    @Order(2)
    void testEventNotFired(TestContext ctx) {
        var capture = ctx.captureEvent(PlayerDisconnectEvent.class);

        EventAssert.assertEventNotFired(capture);
    }

    @WorldTest
    @DisplayName("Cancelled event is detected as cancelled")
    @Order(3)
    void testEventCancellation(WorldTestContext ctx) {
        var capture = ctx.captureEvent(PlaceBlockEvent.class);

        ctx.setBlock(10, 64, 10, "Rock_Sandstone");
        ctx.flush();

        if (capture.wasFired()) {
            var event = capture.getFirst();
            event.setCancelled(true);
            EventAssert.assertEventCancelled(event);
        }
    }

    @WorldTest
    @DisplayName("Event fires exactly once per trigger")
    @Order(4)
    void testEventFireCount(WorldTestContext ctx) {
        var capture = ctx.captureEvent(EntityRemoveEvent.class);

        var entity = ctx.spawnEntity("Kweebec_Sapling", 5, 64, 5);
        ctx.flush();
        ctx.despawn(entity);

        EventAssert.assertEventFired(capture, 1);
    }

    @WorldTest
    @DisplayName("Multiple event captures only catch their own type")
    @Order(5)
    void testMultipleEventTypes(WorldTestContext ctx) {
        var removeCapture = ctx.captureEvent(EntityRemoveEvent.class);
        var chatCapture = ctx.captureEvent(PlayerChatEvent.class);

        var entity = ctx.spawnEntity("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();
        ctx.despawn(entity);

        EventAssert.assertEventFired(removeCapture);
        EventAssert.assertEventNotFired(chatCapture);
    }
}
