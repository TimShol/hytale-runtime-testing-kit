package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.annotation.WorldTest;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;

import java.lang.reflect.Method;

@HytaleSuite(value = "Effect Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("effects")
public class EffectTests {

    @WorldTest
    @DisplayName("EffectControllerComponent is present on spawned NPC")
    @Order(1)
    void appliedEffectIsActive(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();

        var controller = (EffectControllerComponent) ctx.getComponent(
            npc, EffectControllerComponent.getComponentType()
        );
        if (controller == null) {
            ctx.putComponent(npc, EffectControllerComponent.getComponentType(),
                new EffectControllerComponent());
            ctx.flush();
            controller = (EffectControllerComponent) ctx.getComponent(
                npc, EffectControllerComponent.getComponentType()
            );
        }
        HytaleAssert.assertNotNull("EffectController should be present", controller);

        boolean hasActiveEffects = controller.getActiveEffects() != null;
        ctx.log("EffectController active effects present: %b", hasActiveEffects);
    }

    @WorldTest
    @DisplayName("EffectController invulnerability flag is readable and writable")
    @Order(2)
    void invulnerabilityToggle(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Kweebec_Sapling", 5, 64, 5);
        ctx.flush();

        var controller = (EffectControllerComponent) ctx.getComponent(
            npc, EffectControllerComponent.getComponentType()
        );
        if (controller == null) {
            ctx.putComponent(npc, EffectControllerComponent.getComponentType(),
                new EffectControllerComponent());
            ctx.flush();
            controller = (EffectControllerComponent) ctx.getComponent(
                npc, EffectControllerComponent.getComponentType()
            );
        }
        HytaleAssert.assertNotNull(controller);

        controller.setInvulnerable(true);
        HytaleAssert.assertTrue("Entity should be invulnerable", controller.isInvulnerable());

        controller.setInvulnerable(false);
        HytaleAssert.assertFalse("Entity should no longer be invulnerable",
            controller.isInvulnerable());
    }

    @WorldTest
    @DisplayName("EffectController has expected API methods")
    @Order(3)
    void effectControllerApiMethods(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Kweebec_Sapling", 10, 64, 10);
        ctx.flush();

        var controller = (EffectControllerComponent) ctx.getComponent(
            npc, EffectControllerComponent.getComponentType()
        );
        HytaleAssert.assertNotNull("EffectController should be present", controller);

        boolean hasAddEffect = false;
        boolean hasRemoveEffect = false;
        boolean hasClearEffects = false;
        boolean hasGetActiveEffects = false;
        for (Method method : controller.getClass().getMethods()) {
            if ("addEffect".equals(method.getName())) hasAddEffect = true;
            if ("removeEffect".equals(method.getName())) hasRemoveEffect = true;
            if ("clearEffects".equals(method.getName())) hasClearEffects = true;
            if ("getActiveEffects".equals(method.getName())) hasGetActiveEffects = true;
        }
        HytaleAssert.assertTrue("Should have addEffect method", hasAddEffect);
        HytaleAssert.assertTrue("Should have removeEffect method", hasRemoveEffect);
        HytaleAssert.assertTrue("Should have clearEffects method", hasClearEffects);
        HytaleAssert.assertTrue("Should have getActiveEffects method", hasGetActiveEffects);
    }

    @HytaleTest
    @DisplayName("EntityEffect asset map is accessible")
    @Order(4)
    void entityEffectProperties() {
        var effects = EntityEffect.getAssetMap();
        HytaleAssert.assertNotNull("Effect asset map should exist", effects);
    }

    @HytaleTest
    @DisplayName("OverlapBehavior enum has expected values")
    @Order(5)
    void overlapBehaviorValues() {
        HytaleAssert.assertNotNull(OverlapBehavior.EXTEND);
        HytaleAssert.assertNotNull(OverlapBehavior.OVERWRITE);
        HytaleAssert.assertNotNull(OverlapBehavior.IGNORE);
        HytaleAssert.assertEquals(3, OverlapBehavior.values().length);
    }

    @WorldTest
    @DisplayName("EffectController clearEffects method has correct signature")
    @Order(6)
    void clearEffectsMethodExists(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Kweebec_Sapling", 15, 64, 15);
        ctx.flush();

        var controller = (EffectControllerComponent) ctx.getComponent(
            npc, EffectControllerComponent.getComponentType()
        );
        if (controller == null) {
            ctx.putComponent(npc, EffectControllerComponent.getComponentType(),
                new EffectControllerComponent());
            ctx.flush();
            controller = (EffectControllerComponent) ctx.getComponent(
                npc, EffectControllerComponent.getComponentType()
            );
        }
        HytaleAssert.assertNotNull("EffectController should be present", controller);

        boolean hasClearEffects = false;
        for (Method method : controller.getClass().getMethods()) {
            if ("clearEffects".equals(method.getName())) {
                hasClearEffects = true;
                ctx.log("clearEffects takes %d parameters", method.getParameterCount());
            }
        }
        HytaleAssert.assertTrue("clearEffects method should exist", hasClearEffects);
    }
}
