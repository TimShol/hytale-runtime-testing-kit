package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.Benchmark;
import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.context.BenchmarkContext;
import com.frotty27.hrtk.api.context.EcsTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;

@HytaleSuite(value = "Performance Benchmarks", isolation = IsolationStrategy.SNAPSHOT)
@Tag("benchmark")
public class BenchmarkTests {

    @Benchmark(warmup = 50, iterations = 1000)
    @DisplayName("Entity creation throughput")
    @Order(1)
    void benchEntityCreation(BenchmarkContext bctx, EcsTestContext ctx) {
        bctx.startTimer();
        var entity = ctx.createEntity();
        var transform = new TransformComponent(
            new Vector3d(0, 64, 0), new Vector3f(0f, 0f, 0f)
        );
        ctx.putComponent(entity, TransformComponent.getComponentType(), transform);
        ctx.flush();
        bctx.stopTimer();
    }

    @Benchmark(warmup = 100, iterations = 5000)
    @DisplayName("ItemStack creation throughput")
    @Order(2)
    void benchItemStackCreation(BenchmarkContext ctx) {
        ctx.startTimer();
        var stack = new ItemStack("Weapon_Sword_Iron", 1);
        var id = stack.getItemId();
        var qty = stack.getQuantity();
        ctx.stopTimer();

        if (id == null || qty < 0) {
            throw new AssertionError("Unexpected null or negative");
        }
    }

    @Benchmark(warmup = 20, iterations = 500)
    @DisplayName("Container fill and read (27 slots)")
    @Order(3)
    void benchContainerOperations(BenchmarkContext ctx) {
        var container = new SimpleItemContainer((short) 27);

        ctx.startTimer();
        for (short i = 0; i < 27; i++) {
            container.addItemStackToSlot(i, new ItemStack("Wood_Oak_Trunk", 64));
        }
        for (short i = 0; i < 27; i++) {
            var stack = container.getItemStack(i);
            if (stack == null) throw new AssertionError("Null at slot " + i);
        }
        container.clear();
        ctx.stopTimer();
    }

    @Benchmark(warmup = 100, iterations = 10000)
    @DisplayName("Codec round-trip throughput")
    @Order(4)
    void benchCodecRoundTrip(BenchmarkContext ctx) {
        ctx.startTimer();
        var encoded = Codec.STRING.encode("Trork_Warrior");
        var decoded = Codec.STRING.decode(encoded);
        ctx.stopTimer();

        if (decoded == null) {
            throw new AssertionError("Decoded value was null");
        }
    }
}
