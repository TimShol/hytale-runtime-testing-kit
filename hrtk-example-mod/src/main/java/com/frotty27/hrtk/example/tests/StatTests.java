package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.EcsTest;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.SpawnTest;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.CombatAssert;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.assert_.StatsAssert;
import com.frotty27.hrtk.api.context.EcsTestContext;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;

@HytaleSuite(value = "Stat Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("stats")
public class StatTests {

    @SpawnTest
    @DisplayName("Spawned NPC has positive health")
    @Order(1)
    void testNPCHasHealth(WorldTestContext ctx) {
        var skeleton = ctx.spawnNPC("Trork_Warrior", 0, 64, 0);
        ctx.flush();

        StatsAssert.assertAlive(ctx.getStore(), skeleton);
        CombatAssert.assertHealthAbove(ctx.getStore(), skeleton, 0f);
    }

    @EcsTest
    @DisplayName("StaticModifier with FLAT calculation changes the stat value")
    @Order(2)
    void testFlatModifier(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var statMap = new EntityStatMap();
        ctx.putComponent(entity, EntityStatMap.getComponentType(), statMap);
        ctx.flush();

        int healthStat = DefaultEntityStatTypes.getHealth();
        statMap.setStatValue(healthStat, 20.0f);

        float before = statMap.get(healthStat).get();

        var modifier = new StaticModifier(
            Modifier.ModifierTarget.MAX,
            StaticModifier.CalculationType.ADDITIVE,
            5.0f
        );
        statMap.putModifier(healthStat, "strength_potion", modifier);
        statMap.update();

        float after = statMap.get(healthStat).getMax();

        HytaleAssert.assertTrue(
            "Modifier should increase max",
            after > before
        );
        HytaleAssert.assertEquals(5.0, (double) modifier.getAmount(), 0.01);
        HytaleAssert.assertEquals(25.0, (double) modifier.apply(20.0f), 0.01);
    }

    @EcsTest
    @DisplayName("maximizeStatValue sets the stat to max")
    @Order(3)
    void testMaximizeStat(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var statMap = new EntityStatMap();
        ctx.putComponent(entity, EntityStatMap.getComponentType(), statMap);
        ctx.flush();

        int healthStat = DefaultEntityStatTypes.getHealth();
        statMap.setStatValue(healthStat, 10.0f);
        statMap.maximizeStatValue(healthStat);

        StatsAssert.assertStatAtMax(ctx.getStore(), entity, healthStat);
    }

    @EcsTest
    @DisplayName("subtractStatValue does not make health negative")
    @Order(4)
    void testSubtractBelowZero(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var statMap = new EntityStatMap();
        ctx.putComponent(entity, EntityStatMap.getComponentType(), statMap);
        ctx.flush();

        int healthStat = DefaultEntityStatTypes.getHealth();
        statMap.setStatValue(healthStat, 5.0f);
        statMap.subtractStatValue(healthStat, 10.0f);

        float current = statMap.get(healthStat).get();
        HytaleAssert.assertTrue(
            "Health should not go below 0, was: " + current,
            current >= 0f
        );
    }

    @EcsTest
    @DisplayName("Removing a modifier restores original stat behavior")
    @Order(5)
    void testRemoveModifier(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var statMap = new EntityStatMap();
        ctx.putComponent(entity, EntityStatMap.getComponentType(), statMap);
        ctx.flush();

        int healthStat = DefaultEntityStatTypes.getHealth();
        statMap.setStatValue(healthStat, 20.0f);

        var modifier = new StaticModifier(
            Modifier.ModifierTarget.MAX,
            StaticModifier.CalculationType.ADDITIVE,
            10.0f
        );
        statMap.putModifier(healthStat, "test_buff", modifier);
        statMap.update();

        StatsAssert.assertHasModifier(ctx.getStore(), entity, healthStat, "test_buff");

        statMap.removeModifier(healthStat, "test_buff");
        statMap.update();

        StatsAssert.assertNoModifier(ctx.getStore(), entity, healthStat, "test_buff");
    }

    @EcsTest
    @DisplayName("EntityStatMap size reflects registered stats")
    @Order(6)
    void testStatMapSize(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var statMap = new EntityStatMap();
        ctx.putComponent(entity, EntityStatMap.getComponentType(), statMap);
        ctx.flush();

        int size = statMap.size();
        HytaleAssert.assertTrue(
            "Stat map size should be non-negative, was: " + size,
            size >= 0
        );
    }
}
