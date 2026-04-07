package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.FlowTest;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.CombatAssert;
import com.frotty27.hrtk.api.assert_.EcsAssert;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.assert_.ItemAssert;
import com.frotty27.hrtk.api.assert_.NPCAssert;
import com.frotty27.hrtk.api.assert_.StatsAssert;
import com.frotty27.hrtk.api.assert_.WorldAssert;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;

@HytaleSuite(value = "Multi-System Flow Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("flow")
public class FlowTests {

    @FlowTest
    @DisplayName("Spawn, damage, and verify health reduction")
    @Order(1)
    void testSpawnDamageFlow(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Trork_Warrior", 0, 64, 0);
        ctx.flush();

        CombatAssert.assertAlive(ctx.getStore(), npc);
        StatsAssert.assertHealthAtMax(ctx.getStore(), npc);

        var statMap = (EntityStatMap) ctx.getComponent(npc, EntityStatMap.getComponentType());
        HytaleAssert.assertNotNull("Entity should have EntityStatMap", statMap);

        int healthStat = DefaultEntityStatTypes.getHealth();
        statMap.subtractStatValue(healthStat, 5.0f);
        ctx.flush();

        CombatAssert.assertHealthBelow(
            ctx.getStore(), npc,
            statMap.get(healthStat).getMax()
        );
        CombatAssert.assertAlive(ctx.getStore(), npc);
    }

    @HytaleTest
    @DisplayName("Full inventory flow: create item, store, retrieve")
    @Order(2)
    void testInventoryFlow() {
        var container = new SimpleItemContainer((short) 27);
        var sword = new ItemStack("Weapon_Sword_Iron", 1);
        var arrows = new ItemStack("Ammo_Arrow", 64);

        container.addItemStackToSlot((short) 0, sword);
        container.addItemStackToSlot((short) 1, arrows);

        var slot0 = container.getItemStack((short) 0);
        var slot1 = container.getItemStack((short) 1);

        ItemAssert.assertItemId(slot0, "Weapon_Sword_Iron");
        ItemAssert.assertItemQuantity(slot0, 1);
        ItemAssert.assertItemId(slot1, "Ammo_Arrow");
        ItemAssert.assertItemQuantity(slot1, 64);

        container.removeItemStackFromSlot((short) 0);
        HytaleAssert.assertTrue("Container should not be empty after partial remove", !container.isEmpty());

        container.removeItemStackFromSlot((short) 1);
        HytaleAssert.assertTrue("Container should be empty after full remove", container.isEmpty());
    }

    @FlowTest
    @DisplayName("NPC spawn with full validation")
    @Order(3)
    void testNPCFullSpawn(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Trork_Warrior", 50, 64, 50);
        ctx.flush();

        EcsAssert.assertRefValid(npc);
        HytaleAssert.assertTrue("NPC should exist", ctx.entityExists(npc));

        double[] pos = ctx.getPosition(npc);
        HytaleAssert.assertNotNull("NPC should have a position", pos);
        HytaleAssert.assertEquals(50.0, pos[0], 2.0);

        NPCAssert.assertNPCEntity(ctx.getStore(), npc);
        NPCAssert.assertNotDespawning(ctx.getStore(), npc);

        StatsAssert.assertAlive(ctx.getStore(), npc);
    }

    @FlowTest
    @DisplayName("Block placement near entity changes world state")
    @Order(4)
    void testBlockEntityInteraction(WorldTestContext ctx) {
        ctx.setBlock(10, 64, 10, "Rock_Stone");
        ctx.setBlock(10, 65, 10, "Empty");

        var npc = ctx.spawnNPC("Kweebec_Sapling", 10, 65, 10);
        ctx.flush();

        WorldAssert.assertBlockAt(ctx.getWorld(), 10, 64, 10, "Rock_Stone");
        EcsAssert.assertRefValid(npc);
        HytaleAssert.assertTrue("NPC should exist above block", ctx.entityExists(npc));
    }

    @FlowTest
    @DisplayName("Message system creates and formats text correctly")
    @Order(5)
    void testMessageSystem(WorldTestContext ctx) {
        var message = Message.raw("Hello");
        HytaleAssert.assertNotNull("Message should not be null", message);

        var rawText = message.getRawText();
        HytaleAssert.assertNotNull("Raw text should not be null", rawText);
        HytaleAssert.assertEquals("Hello", rawText);

        var emptyMessage = Message.empty();
        HytaleAssert.assertNotNull("Empty message should not be null", emptyMessage);
    }
}
