package com.frotty27.hrtk.server.context;

import com.frotty27.hrtk.api.context.EcsTestContext;
import com.frotty27.hrtk.server.isolation.TestEntityTracker;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class LiveEcsTestContext extends LiveTestContext implements EcsTestContext {

    private final Store<EntityStore> store;
    private final CommandBuffer<EntityStore> commandBuffer;
    private final World world;
    private TestEntityTracker entityTracker;

    public LiveEcsTestContext(String pluginName, Store<EntityStore> store,
                              CommandBuffer<EntityStore> commandBuffer) {
        super(pluginName);
        this.store = store;
        this.commandBuffer = commandBuffer;
        this.world = resolveWorld();
    }

    private World resolveWorld() {
        try {
            Map<String, World> worlds = Universe.get().getWorlds();
            if (worlds != null && !worlds.isEmpty()) {
                return worlds.values().iterator().next();
            }
        } catch (Exception _) {}
        return null;
    }

    public World getWorld() { return world; }

    public void setEntityTracker(TestEntityTracker entityTracker) {
        this.entityTracker = entityTracker;
    }

    private boolean isOnWorldThread() {
        return world != null && world.isInThread();
    }

    private <T> T onWorldThread(Supplier<T> action) {
        if (world == null || isOnWorldThread()) return action.get();
        CompletableFuture<T> future = new CompletableFuture<>();
        world.execute(() -> {
            try { future.complete(action.get()); }
            catch (Throwable throwable) { future.completeExceptionally(throwable); }
        });
        try { return future.get(30, TimeUnit.SECONDS); }
        catch (ExecutionException e) { throw new RuntimeException(e.getCause()); }
        catch (Exception e) { throw new RuntimeException("World thread dispatch failed", e); }
    }

    private void runOnWorldThread(Runnable action) {
        if (world == null || isOnWorldThread()) { action.run(); return; }
        CompletableFuture<Void> future = new CompletableFuture<>();
        world.execute(() -> {
            try { action.run(); future.complete(null); }
            catch (Throwable throwable) { future.completeExceptionally(throwable); }
        });
        try { future.get(30, TimeUnit.SECONDS); }
        catch (ExecutionException e) { throw new RuntimeException(e.getCause()); }
        catch (Exception e) { throw new RuntimeException("World thread dispatch failed", e); }
    }

    @Override
    public Store<EntityStore> getStore() { return store; }

    @Override
    public CommandBuffer<EntityStore> getCommandBuffer() { return commandBuffer; }

    @Override
    public Ref<EntityStore> createEntity() {
        return onWorldThread(() -> {
            Ref<EntityStore> ref = store.addEntity(Archetype.empty(), AddReason.SPAWN);
            if (entityTracker != null) entityTracker.trackEntity(ref);
            return ref;
        });
    }

    @Override
    public void flush() {
        if (commandBuffer == null) return;
        runOnWorldThread(() -> {
            try {
                for (var method : commandBuffer.getClass().getMethods()) {
                    if ("drain".equals(method.getName()) && method.getParameterCount() == 0) {
                        method.invoke(commandBuffer);
                        return;
                    }
                }
            } catch (Exception _) {}
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void putComponent(Object entityRef, Object componentType, Object component) {
        runOnWorldThread(() -> store.putComponent(
                (Ref<EntityStore>) entityRef,
                (ComponentType<EntityStore, Component<EntityStore>>) componentType,
                (Component<EntityStore>) component));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeComponent(Object entityRef, Object componentType) {
        runOnWorldThread(() -> store.removeComponent(
                (Ref<EntityStore>) entityRef,
                (ComponentType<EntityStore, ? extends Component<EntityStore>>) componentType));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getComponent(Object entityRef, Object componentType) {
        return onWorldThread(() -> store.getComponent(
                (Ref<EntityStore>) entityRef,
                (ComponentType<EntityStore, ? extends Component<EntityStore>>) componentType));
    }

    @Override
    public boolean hasComponent(Object entityRef, Object componentType) {
        return onWorldThread(() -> getComponent(entityRef, componentType) != null);
    }


    @Override
    public void waitTicks(int ticks) {
        if (ticks <= 0 || world == null) return;
        if (isOnWorldThread()) {
            throw new RuntimeException(
                    "waitTicks cannot be called from the world thread. "
                    + "Use @FlowTest or @AsyncTest instead of @EcsTest for tests that wait for ticks.");
        }
        long targetTick = world.getTick() + ticks;
        long deadline = System.currentTimeMillis() + ticks * 1000L;
        while (world.getTick() < targetTick) {
            if (System.currentTimeMillis() > deadline) {
                throw new RuntimeException("waitTicks(" + ticks + ") timed out");
            }
            try { Thread.sleep(25); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("waitTicks interrupted", e);
            }
        }
    }

    @Override
    public CompletableFuture<Void> waitTicksAsync(int ticks) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (ticks <= 0 || world == null) {
            future.complete(null);
            return future;
        }
        long targetTick = world.getTick() + ticks;
        CompletableFuture.runAsync(() -> {
            long deadline = System.currentTimeMillis() + ticks * 1000L;
            while (world.getTick() < targetTick) {
                if (System.currentTimeMillis() > deadline) {
                    future.completeExceptionally(new RuntimeException("waitTicksAsync timed out"));
                    return;
                }
                try { Thread.sleep(25); } catch (InterruptedException e) {
                    future.completeExceptionally(e);
                    return;
                }
            }
            future.complete(null);
        });
        return future;
    }

    @Override
    public <T> T awaitCondition(Supplier<T> condition, int maxTicks) {
        return awaitCondition(condition, maxTicks, "Condition not met within " + maxTicks + " ticks");
    }

    @Override
    public <T> T awaitCondition(Supplier<T> condition, int maxTicks, String failMessage) {
        if (world == null) throw new RuntimeException("No world available for awaitCondition");
        if (isOnWorldThread()) {
            throw new RuntimeException(
                    "awaitCondition cannot be called from the world thread. "
                    + "Use @FlowTest or @AsyncTest instead of @EcsTest for tests that wait for ticks.");
        }
        long targetTick = world.getTick() + maxTicks;
        long deadline = System.currentTimeMillis() + maxTicks * 1000L;
        while (world.getTick() <= targetTick) {
            try {
                T value = condition.get();
                if (value != null) return value;
            } catch (Exception _) {}
            if (System.currentTimeMillis() > deadline) {
                throw new RuntimeException(failMessage + " (timeout)");
            }
            try { Thread.sleep(25); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("awaitCondition interrupted", e);
            }
        }
        throw new RuntimeException(failMessage);
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<?> findEntities(Object componentType) {
        return onWorldThread(() -> {
            List<Ref<EntityStore>> results = new ArrayList<>();
            ComponentType<EntityStore, ?> targetType = (ComponentType<EntityStore, ?>) componentType;
            try {
                var refsField = Store.class.getDeclaredField("refs");
                refsField.setAccessible(true);
                Ref<EntityStore>[] refs = (Ref<EntityStore>[]) refsField.get(store);
                var sizeField = Store.class.getDeclaredField("entitiesSize");
                sizeField.setAccessible(true);
                int size = (int) sizeField.get(store);

                for (int i = 0; i < size; i++) {
                    if (refs[i] != null && refs[i].isValid()) {
                        Archetype<EntityStore> arch = store.getArchetype(refs[i]);
                        if (arch != null && arch.contains(targetType)) {
                            results.add(refs[i]);
                        }
                    }
                }
            } catch (Exception e) {
                log("findEntities failed: %s", e.getMessage());
            }
            return results;
        });
    }

    @Override
    public int countEntities(Object componentType) {
        return findEntities(componentType).size();
    }
}
