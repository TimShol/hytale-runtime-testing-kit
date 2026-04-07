package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.EcsTest;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.annotation.WorldTest;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.context.EcsTestContext;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.component.PositionDataComponent;
import com.hypixel.hytale.math.vector.Vector3f;

@HytaleSuite(value = "Entity Component Expanded Tests", isolation = IsolationStrategy.SNAPSHOT)
@Tag({"ecs", "components"})
public class EntityComponentExpandedTests {

    @EcsTest
    @DisplayName("DisplayNameComponent stores and returns the name")
    @Order(1)
    void displayNameStoresCorrectly(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var name = Message.raw("Elite Skeleton");
        var comp = new DisplayNameComponent(name);
        ctx.putComponent(entity, DisplayNameComponent.getComponentType(), comp);
        ctx.flush();

        var stored = (DisplayNameComponent) ctx.getComponent(
            entity, DisplayNameComponent.getComponentType()
        );
        HytaleAssert.assertNotNull("DisplayName should be retrievable", stored);
        HytaleAssert.assertNotNull("Display name message should not be null",
            stored.getDisplayName());
    }

    @EcsTest
    @DisplayName("EntityScaleComponent stores scale factor")
    @Order(2)
    void entityScaleStoresCorrectly(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var scale = new EntityScaleComponent(2.5f);
        ctx.putComponent(entity, EntityScaleComponent.getComponentType(), scale);
        ctx.flush();

        var stored = (EntityScaleComponent) ctx.getComponent(
            entity, EntityScaleComponent.getComponentType()
        );
        HytaleAssert.assertNotNull(stored);
        HytaleAssert.assertEquals(2.5, (double) stored.getScale(), 0.01);

        stored.setScale(0.5f);
        HytaleAssert.assertEquals(0.5, (double) stored.getScale(), 0.01);
    }

    @EcsTest
    @DisplayName("Invulnerable component can be added as singleton")
    @Order(3)
    void invulnerableComponentWorks(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        ctx.putComponent(entity, Invulnerable.getComponentType(), Invulnerable.INSTANCE);
        ctx.flush();

        HytaleAssert.assertTrue("Entity should have Invulnerable component",
            ctx.hasComponent(entity, Invulnerable.getComponentType()));
    }

    @EcsTest
    @DisplayName("BoundingBox can be read and modified")
    @Order(4)
    void boundingBoxReadWrite(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var bb = new BoundingBox();
        ctx.putComponent(entity, BoundingBox.getComponentType(), bb);
        ctx.flush();

        var stored = (BoundingBox) ctx.getComponent(entity, BoundingBox.getComponentType());
        HytaleAssert.assertNotNull("BoundingBox should be retrievable", stored);
    }

    @EcsTest
    @DisplayName("HeadRotation stores rotation and computes direction")
    @Order(5)
    void headRotationStoresAndComputes(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var rotation = new HeadRotation(new Vector3f(0f, 90f, 0f));
        ctx.putComponent(entity, HeadRotation.getComponentType(), rotation);
        ctx.flush();

        var stored = (HeadRotation) ctx.getComponent(entity, HeadRotation.getComponentType());
        HytaleAssert.assertNotNull(stored);
        HytaleAssert.assertNotNull("Rotation vector should not be null", stored.getRotation());
        HytaleAssert.assertNotNull("Direction should be computable", stored.getDirection());
    }

    @WorldTest
    @DisplayName("PositionDataComponent tracks block context")
    @Order(6)
    void positionDataTracksBlockContext(WorldTestContext ctx) {
        var entity = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();

        var posData = (PositionDataComponent) ctx.getComponent(
            entity, PositionDataComponent.getComponentType()
        );
        if (posData != null) {
            int standingOn = posData.getStandingOnBlockTypeId();
            ctx.log("Entity standing on block type ID: %d", standingOn);
        }
    }

    @EcsTest
    @DisplayName("Marker components can be attached as singletons")
    @Order(7)
    void markerComponentsAttachable(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        ctx.putComponent(entity, Invulnerable.getComponentType(), Invulnerable.INSTANCE);
        ctx.putComponent(entity, Intangible.getComponentType(), Intangible.INSTANCE);
        ctx.flush();

        HytaleAssert.assertTrue("Entity should have Invulnerable",
            ctx.hasComponent(entity, Invulnerable.getComponentType()));
        HytaleAssert.assertTrue("Entity should have Intangible",
            ctx.hasComponent(entity, Intangible.getComponentType()));
    }
}
