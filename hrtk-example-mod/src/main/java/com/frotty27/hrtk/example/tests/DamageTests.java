package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.CombatTest;
import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.CombatAssert;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;

@HytaleSuite(value = "Damage Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("combat")
public class DamageTests {

    @HytaleTest
    @DisplayName("Damage object stores amount and cause correctly")
    @Order(1)
    void testDamageConstruction() {
        var damage = new Damage(Damage.NULL_SOURCE, DamageCause.PHYSICAL, 25.0f);

        HytaleAssert.assertEquals(25.0, (double) damage.getAmount(), 0.01);
        HytaleAssert.assertEquals(25.0, (double) damage.getInitialAmount(), 0.01);
        HytaleAssert.assertNotNull(damage.getSource());
    }

    @HytaleTest
    @DisplayName("DamageCause properties match expected behavior")
    @Order(2)
    void testDamageCauseProperties() {
        HytaleAssert.assertFalse(
            "Fall damage should not cause durability loss",
            DamageCause.FALL.isDurabilityLoss()
        );
        HytaleAssert.assertTrue(
            "Physical damage should cause durability loss",
            DamageCause.PHYSICAL.isDurabilityLoss()
        );
        HytaleAssert.assertTrue(
            "Command damage should bypass resistances",
            DamageCause.COMMAND.doesBypassResistances()
        );
        HytaleAssert.assertFalse(
            "Physical damage should NOT bypass resistances",
            DamageCause.PHYSICAL.doesBypassResistances()
        );
    }

    @CombatTest
    @DisplayName("Lethal damage kills the target")
    @Order(3)
    void testLethalDamage(WorldTestContext ctx) {
        var target = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();

        CombatAssert.assertAlive(ctx.getStore(), target);

        var damage = new Damage(
            Damage.NULL_SOURCE,
            DamageCause.COMMAND,
            9999.0f
        );
        var statMap = (EntityStatMap) ctx.getComponent(target, EntityStatMap.getComponentType());
        int healthStat = DefaultEntityStatTypes.getHealth();
        statMap.subtractStatValue(healthStat, damage.getAmount());
        ctx.flush();

        CombatAssert.assertHealthBelow(ctx.getStore(), target, 1.0f);
    }

    @HytaleTest
    @DisplayName("Drowning and environment damage types have valid IDs and no durability loss")
    @Order(4)
    void testDrowningAndEnvironmentTypes() {
        HytaleAssert.assertNotNull(
            "DROWNING should have an ID",
            DamageCause.DROWNING.getId()
        );
        HytaleAssert.assertNotNull(
            "ENVIRONMENT should have an ID",
            DamageCause.ENVIRONMENT.getId()
        );
        HytaleAssert.assertFalse(
            "Drowning damage should not cause durability loss",
            DamageCause.DROWNING.isDurabilityLoss()
        );
        HytaleAssert.assertFalse(
            "Environment damage should not cause durability loss",
            DamageCause.ENVIRONMENT.isDurabilityLoss()
        );
    }
}
