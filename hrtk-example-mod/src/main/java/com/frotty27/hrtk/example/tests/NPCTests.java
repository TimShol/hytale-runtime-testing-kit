package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.SpawnTest;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.EcsAssert;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.assert_.NPCAssert;
import com.frotty27.hrtk.api.assert_.StatsAssert;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.npc.NPCPlugin;

@HytaleSuite(value = "NPC Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("npc")
public class NPCTests {

    @SpawnTest
    @DisplayName("Spawn NPC with role and verify NPCEntity component")
    @Order(1)
    void testSpawnNPCWithRole(WorldTestContext ctx) {
        var skeleton = ctx.spawnNPC("Trork_Warrior", 0, 64, 0);
        ctx.flush();

        EcsAssert.assertRefValid(skeleton);
        NPCAssert.assertNPCEntity(ctx.getStore(), skeleton);
        HytaleAssert.assertTrue("NPC should exist", ctx.entityExists(skeleton));
    }

    @HytaleTest
    @DisplayName("NPC role names are registered in NPCPlugin")
    @Order(2)
    void testRoleNamesExist() {
        NPCAssert.assertRoleExists("Trork_Warrior");
        NPCAssert.assertRoleExists("Kweebec_Sapling");
    }

    @HytaleTest
    @DisplayName("NPCPlugin singleton is available and has roles")
    @Order(3)
    void testNPCPluginAccess() {
        var plugin = NPCPlugin.get();
        HytaleAssert.assertNotNull("NPCPlugin.get() should not be null", plugin);

        var roles = plugin.getRoleTemplateNames(false);
        HytaleAssert.assertNotNull("Role template list should not be null", roles);
        HytaleAssert.assertNotEmpty(roles);
    }

    @SpawnTest
    @DisplayName("Freshly spawned NPC is not despawning")
    @Order(4)
    void testNPCNotDespawning(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Trork_Warrior", 0, 64, 0);
        ctx.flush();

        NPCAssert.assertNotDespawning(ctx.getStore(), npc);
    }

    @SpawnTest
    @DisplayName("Spawned NPC has positive health and is at max")
    @Order(5)
    void testNPCHasHealthOnSpawn(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Trork_Warrior", 0, 64, 0);
        ctx.flush();

        StatsAssert.assertAlive(ctx.getStore(), npc);
        StatsAssert.assertHealthAtMax(ctx.getStore(), npc);
    }
}
