package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.annotation.WorldTest;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.assert_.PhysicsAssert;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;

@HytaleSuite(value = "Physics Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("physics")
public class PhysicsTests {

    @WorldTest
    @DisplayName("Velocity can be set and read back")
    @Order(1)
    void testSetVelocity(WorldTestContext ctx) {
        var entity = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();

        var velocity = new Velocity();
        velocity.set(1.0, 5.0, -1.0);
        ctx.putComponent(entity, Velocity.getComponentType(), velocity);
        ctx.flush();

        PhysicsAssert.assertVelocity(
            ctx.getStore(), entity,
            1.0, 5.0, -1.0,
            0.1
        );
    }

    @WorldTest
    @DisplayName("addForce accumulates on existing velocity")
    @Order(2)
    void testAddForce(WorldTestContext ctx) {
        var entity = ctx.spawnNPC("Trork_Warrior", 0, 64, 0);
        ctx.flush();

        var velocity = new Velocity();
        velocity.set(1.0, 0.0, 0.0);
        velocity.addForce(0.0, 5.0, 0.0);
        ctx.putComponent(entity, Velocity.getComponentType(), velocity);
        ctx.flush();

        PhysicsAssert.assertVelocity(
            ctx.getStore(), entity,
            1.0, 5.0, 0.0,
            0.1
        );
    }

    @WorldTest
    @DisplayName("TransformComponent stores position correctly")
    @Order(3)
    void testTransformPosition(WorldTestContext ctx) {
        var entity = ctx.spawnNPC("Kweebec_Sapling", 10, 64, 20);
        ctx.flush();

        var transform = new TransformComponent(
            new Vector3d(100.0, 70.0, 200.0),
            new Vector3f(0f, 90f, 0f)
        );
        ctx.putComponent(entity, TransformComponent.getComponentType(), transform);
        ctx.flush();

        double[] pos = ctx.getPosition(entity);
        HytaleAssert.assertNotNull("Position should not be null", pos);
        HytaleAssert.assertEquals(100.0, pos[0], 1.0);
        HytaleAssert.assertEquals(70.0, pos[1], 1.0);
        HytaleAssert.assertEquals(200.0, pos[2], 1.0);
    }

    @WorldTest
    @DisplayName("setZero makes entity stationary")
    @Order(4)
    void testSetZeroVelocity(WorldTestContext ctx) {
        var entity = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();

        var velocity = new Velocity();
        velocity.set(10.0, 10.0, 10.0);
        velocity.setZero();
        ctx.putComponent(entity, Velocity.getComponentType(), velocity);
        ctx.flush();

        PhysicsAssert.assertStationary(ctx.getStore(), entity, 0.01);
    }

    @WorldTest
    @DisplayName("Entity speed is within expected range after applying forces")
    @Order(5)
    void testSpeedRange(WorldTestContext ctx) {
        var entity = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();

        var velocity = new Velocity();
        velocity.set(3.0, 0.0, 4.0);
        ctx.putComponent(entity, Velocity.getComponentType(), velocity);
        ctx.flush();

        PhysicsAssert.assertSpeed(ctx.getStore(), entity, 4.0, 6.0);
    }

    @WorldTest
    @DisplayName("Entity on solid block reports ground state")
    @Order(6)
    void testGroundState(WorldTestContext ctx) {
        ctx.setBlock(0, 63, 0, "Rock_Stone");

        var entity = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();

        PhysicsAssert.assertOnGround(ctx.getStore(), entity);
    }
}
