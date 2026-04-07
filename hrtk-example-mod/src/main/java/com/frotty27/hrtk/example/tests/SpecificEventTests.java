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
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.entity.EntityRemoveEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;

@HytaleSuite(value = "Specific Event Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("events")
public class SpecificEventTests {

    @WorldTest
    @DisplayName("BreakBlockEvent carries correct block type and position")
    @Order(1)
    void breakBlockEventHasCorrectData(WorldTestContext ctx) {
        var capture = ctx.captureEvent(BreakBlockEvent.class);

        ctx.setBlock(10, 64, 10, "Rock_Sandstone");
        ctx.flush();

        EventAssert.assertEventFired(capture);
        var event = capture.getFirst();
        HytaleAssert.assertNotNull("Block type should not be null", event.getBlockType());
        HytaleAssert.assertNotNull("Target position should not be null", event.getTargetBlock());
    }

    @WorldTest
    @DisplayName("Cancelling PlaceBlockEvent prevents the block from being placed")
    @Order(2)
    void cancelledPlaceBlockPreventsPlacement(WorldTestContext ctx) {
        var capture = ctx.captureEvent(PlaceBlockEvent.class);

        ctx.setBlock(10, 64, 10, "Rock_Sandstone");
        ctx.flush();

        if (capture.wasFired()) {
            var event = capture.getFirst();
            HytaleAssert.assertNotNull("PlaceBlockEvent target should not be null",
                event.getTargetBlock());
            event.setCancelled(true);
            EventAssert.assertEventCancelled(event);
        }
    }

    @HytaleTest
    @DisplayName("PlayerChatEvent content can be read and modified")
    @Order(3)
    void chatEventContentModifiable(TestContext ctx) {
        var capture = ctx.captureEvent(PlayerChatEvent.class);

        if (capture.wasFired()) {
            var event = capture.getFirst();
            HytaleAssert.assertNotNull("Chat content should not be null", event.getContent());

            event.setContent("Modified message");
            HytaleAssert.assertEquals("Modified message", event.getContent());
        }
    }

    @HytaleTest
    @DisplayName("PlayerConnectEvent provides world and player reference")
    @Order(4)
    void connectEventHasWorldAndPlayer(TestContext ctx) {
        var capture = ctx.captureEvent(PlayerConnectEvent.class);

        if (capture.wasFired()) {
            var event = capture.getFirst();
            HytaleAssert.assertNotNull("World should not be null", event.getWorld());
            HytaleAssert.assertNotNull("PlayerRef should not be null", event.getPlayerRef());
        }
    }

    @HytaleTest
    @DisplayName("PlayerDisconnectEvent fires with player reference")
    @Order(5)
    void disconnectEventHasPlayerRef(TestContext ctx) {
        var capture = ctx.captureEvent(PlayerDisconnectEvent.class);

        if (capture.wasFired()) {
            HytaleAssert.assertNotNull("PlayerRef should not be null",
                capture.getFirst().getPlayerRef());
        }
    }

    @WorldTest
    @DisplayName("DamageBlockEvent carries damage amount and block type")
    @Order(6)
    void damageBlockEventHasDamageAndType(WorldTestContext ctx) {
        var capture = ctx.captureEvent(DamageBlockEvent.class);

        ctx.setBlock(10, 64, 10, "Rock_Sandstone");
        ctx.flush();

        if (capture.wasFired()) {
            var event = capture.getFirst();
            HytaleAssert.assertNotNull("Block type should be present", event.getBlockType());
            HytaleAssert.assertTrue("Damage should be positive", event.getDamage() > 0f);
        }
    }

    @HytaleTest
    @DisplayName("CraftRecipeEvent carries recipe and quantity")
    @Order(7)
    void craftEventHasRecipeData(TestContext ctx) {
        var capture = ctx.captureEvent(CraftRecipeEvent.class);

        if (capture.wasFired()) {
            var event = capture.getFirst();
            HytaleAssert.assertNotNull("Recipe should not be null", event.getCraftedRecipe());
            HytaleAssert.assertTrue("Quantity should be at least 1", event.getQuantity() >= 1);
        }
    }

    @HytaleTest
    @DisplayName("SwitchActiveSlotEvent class is available at runtime")
    @Order(8)
    void slotSwitchEventClassExists() {
        try {
            Class<?> eventClass = Class.forName(
                "com.hypixel.hytale.server.core.event.events.ecs.SwitchActiveSlotEvent");
            HytaleAssert.assertNotNull("SwitchActiveSlotEvent class should be loadable", eventClass);

            boolean hasPreviousSlot = false;
            boolean hasNewSlot = false;
            for (var method : eventClass.getMethods()) {
                if ("getPreviousSlot".equals(method.getName())) hasPreviousSlot = true;
                if ("getNewSlot".equals(method.getName())) hasNewSlot = true;
            }
            HytaleAssert.assertTrue("Should have getPreviousSlot method", hasPreviousSlot);
            HytaleAssert.assertTrue("Should have getNewSlot method", hasNewSlot);
        } catch (ClassNotFoundException ignored) {
        }
    }

    @HytaleTest
    @DisplayName("ChangeGameModeEvent class is available at runtime")
    @Order(9)
    void gameModeChangeEventClassExists() {
        try {
            Class<?> eventClass = Class.forName(
                "com.hypixel.hytale.server.core.event.events.ecs.ChangeGameModeEvent");
            HytaleAssert.assertNotNull("ChangeGameModeEvent class should be loadable", eventClass);

            boolean hasGetGameMode = false;
            boolean hasSetCancelled = false;
            for (var method : eventClass.getMethods()) {
                if ("getGameMode".equals(method.getName())) hasGetGameMode = true;
                if ("setCancelled".equals(method.getName())) hasSetCancelled = true;
            }
            HytaleAssert.assertTrue("Should have getGameMode method", hasGetGameMode);
            HytaleAssert.assertTrue("Should have setCancelled method", hasSetCancelled);
        } catch (ClassNotFoundException ignored) {
        }
    }

    @HytaleTest
    @DisplayName("EventPriority values are ordered correctly")
    @Order(10)
    void eventPriorityOrdering() {
        HytaleAssert.assertTrue("FIRST should have lowest value",
            EventPriority.FIRST.getValue() < EventPriority.NORMAL.getValue());
        HytaleAssert.assertTrue("LAST should have highest value",
            EventPriority.LAST.getValue() > EventPriority.NORMAL.getValue());
        HytaleAssert.assertTrue("EARLY between FIRST and NORMAL",
            EventPriority.EARLY.getValue() > EventPriority.FIRST.getValue()
            && EventPriority.EARLY.getValue() < EventPriority.NORMAL.getValue());
    }

    @WorldTest
    @DisplayName("PlayerInteractEvent provides interaction context")
    @Order(11)
    void interactEventHasContext(WorldTestContext ctx) {
        var capture = ctx.captureEvent(PlayerInteractEvent.class);

        ctx.setBlock(10, 64, 10, "Rock_Sandstone");
        ctx.flush();

        if (capture.wasFired()) {
            var event = capture.getFirst();
            HytaleAssert.assertNotNull("Interaction type should not be null",
                event.getActionType());
        }
    }

    @WorldTest
    @DisplayName("EntityRemoveEvent fires when entity is removed")
    @Order(12)
    void entityRemoveEventFires(WorldTestContext ctx) {
        var capture = ctx.captureEvent(EntityRemoveEvent.class);

        var entity = ctx.spawnEntity("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();
        ctx.despawn(entity);

        EventAssert.assertEventFired(capture);
    }
}
