package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.EcsTest;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.annotation.WorldTest;
import com.frotty27.hrtk.api.assert_.EcsAssert;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.assert_.WorldAssert;
import com.frotty27.hrtk.api.context.EcsTestContext;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;

@HytaleSuite(value = "Entity Component Tests", isolation = IsolationStrategy.SNAPSHOT)
@Tag("ecs")
public class EntityComponentTests {

    @EcsTest
    @DisplayName("putComponent stores data that getComponent reads back")
    @Order(1)
    void testPutThenGet(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var transform = new TransformComponent(
            new Vector3d(100.0, 64.0, 200.0),
            new Vector3f(0f, 0f, 0f)
        );
        ctx.putComponent(entity, TransformComponent.getComponentType(), transform);
        ctx.flush();

        EcsAssert.assertHasComponent(ctx.getStore(), entity, TransformComponent.getComponentType());
        var stored = (TransformComponent) EcsAssert.assertGetComponent(
            ctx.getStore(), entity, TransformComponent.getComponentType()
        );
        HytaleAssert.assertNotNull(stored.getPosition());
    }

    @EcsTest
    @DisplayName("removeComponent actually removes the component after flush")
    @Order(2)
    void testRemoveComponent(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var transform = new TransformComponent(
            new Vector3d(0, 64, 0), new Vector3f(0f, 0f, 0f)
        );
        ctx.putComponent(entity, TransformComponent.getComponentType(), transform);
        ctx.flush();

        EcsAssert.assertHasComponent(ctx.getStore(), entity, TransformComponent.getComponentType());

        ctx.removeComponent(entity, TransformComponent.getComponentType());
        ctx.flush();

        EcsAssert.assertNotHasComponent(ctx.getStore(), entity, TransformComponent.getComponentType());
    }

    @WorldTest
    @DisplayName("Spawned entity exists in world")
    @Order(3)
    void testEntityExistsAfterSpawn(WorldTestContext ctx) {
        var ref = ctx.spawnEntity("Kweebec_Sapling", 10.0, 64.0, 10.0);
        ctx.flush();

        EcsAssert.assertRefValid(ref);
        WorldAssert.assertEntityInWorld(ctx.getWorld(), ref);
        HytaleAssert.assertTrue("Entity should exist", ctx.entityExists(ref));
    }

    @WorldTest
    @DisplayName("Entity spawns at the specified coordinates")
    @Order(4)
    void testEntityPosition(WorldTestContext ctx) {
        var ref = ctx.spawnNPC("Trork_Warrior", 50.0, 64.0, 50.0);
        ctx.flush();

        double[] pos = ctx.getPosition(ref);
        HytaleAssert.assertNotNull("Position should not be null", pos);
        HytaleAssert.assertEquals(50.0, pos[0], 1.0);
        HytaleAssert.assertEquals(64.0, pos[1], 2.0);
        HytaleAssert.assertEquals(50.0, pos[2], 1.0);
    }

    @WorldTest
    @DisplayName("Despawning removes the entity from the world")
    @Order(5)
    void testDespawnRemovesEntity(WorldTestContext ctx) {
        var ref = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();

        HytaleAssert.assertTrue("Entity should exist before despawn", ctx.entityExists(ref));

        ctx.despawn(ref);
        ctx.flush();

        HytaleAssert.assertFalse("Entity should not exist after despawn", ctx.entityExists(ref));
    }

    @EcsTest
    @DisplayName("Entity count changes by exactly 1 after creating an entity")
    @Order(6)
    void testEntityCountQueries(EcsTestContext ctx) {
        int before = ctx.countEntities(TransformComponent.getComponentType());

        var entity = ctx.createEntity();
        var transform = new TransformComponent(
            new Vector3d(0, 64, 0), new Vector3f(0f, 0f, 0f)
        );
        ctx.putComponent(entity, TransformComponent.getComponentType(), transform);
        ctx.flush();

        int after = ctx.countEntities(TransformComponent.getComponentType());
        HytaleAssert.assertEquals(before + 1, after);
    }
}
