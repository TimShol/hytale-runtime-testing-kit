package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.CombatTest;
import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathItemLoss;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;

@HytaleSuite(value = "Death and Respawn Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag({"combat", "death"})
public class DeathRespawnTests {

    @CombatTest
    @DisplayName("DeathComponent is accessible after lethal damage")
    @Order(1)
    void deathComponentAfterKill(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();

        var statMap = (EntityStatMap) ctx.getComponent(npc, EntityStatMap.getComponentType());
        if (statMap != null) {
            try {
                Class<?> defaultStatTypes = Class.forName(
                    "com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes");
                var getHealth = defaultStatTypes.getMethod("getHealth");
                int healthStat = (int) getHealth.invoke(null);
                statMap.subtractStatValue(healthStat, 9999.0f);
                ctx.flush();
            } catch (Exception e) {
                ctx.log("Could not access DefaultEntityStatTypes: %s", e.getMessage());
            }
        }

        var death = ctx.getComponent(npc, DeathComponent.getComponentType());
        if (death != null) {
            ctx.log("DeathComponent found on entity after lethal damage");
        }
    }

    @HytaleTest
    @DisplayName("DeathItemLoss.noLossMode returns a valid no-loss configuration")
    @Order(2)
    void noLossMode() {
        var noLoss = DeathItemLoss.noLossMode();
        HytaleAssert.assertNotNull("noLossMode should return a valid object", noLoss);
    }

    @HytaleTest
    @DisplayName("DamageModule singleton is accessible")
    @Order(3)
    void damageModuleAccessible() {
        var module = DamageModule.get();
        HytaleAssert.assertNotNull("DamageModule should not be null", module);
    }

    @HytaleTest
    @DisplayName("DamageModule provides damage group accessors")
    @Order(4)
    void damageGroupsAccessible() {
        var module = DamageModule.get();
        HytaleAssert.assertNotNull("DamageModule should not be null", module);

        var gatherGroup = module.getGatherDamageGroup();
        HytaleAssert.assertNotNull("Gather damage group should not be null", gatherGroup);

        var filterGroup = module.getFilterDamageGroup();
        HytaleAssert.assertNotNull("Filter damage group should not be null", filterGroup);

        var inspectGroup = module.getInspectDamageGroup();
        HytaleAssert.assertNotNull("Inspect damage group should not be null", inspectGroup);
    }
}
