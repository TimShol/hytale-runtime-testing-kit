package com.frotty27.hrtk.server.context;

import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.server.isolation.TestEntityTracker;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.NewSpawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class LiveWorldTestContext extends LiveTestContext implements WorldTestContext {

    private final World world;
    private final Store<EntityStore> store;
    private CommandBuffer<EntityStore> commandBuffer;
    private TestEntityTracker entityTracker;

    public LiveWorldTestContext(String pluginName, World world) {
        super(pluginName);
        this.world = world;
        EntityStore entityStore = world.getEntityStore();
        this.store = entityStore.getStore();

        try {
            @SuppressWarnings("rawtypes")
            var ctor = CommandBuffer.class.getDeclaredConstructor(Store.class);
            ctor.setAccessible(true);
            @SuppressWarnings("unchecked")
            CommandBuffer<EntityStore> buffer = ctor.newInstance(store);
            this.commandBuffer = buffer;
        } catch (Exception e) {
            log("Warning: Could not create CommandBuffer: %s", e.getMessage());
        }
    }

    public void setEntityTracker(TestEntityTracker entityTracker) {
        this.entityTracker = entityTracker;
    }

    @Override
    public <T> T executeOnWorld(java.util.function.Supplier<T> action) {
        return onWorldThread(action);
    }

    @Override
    public void runOnWorld(Runnable action) {
        runOnWorldThread(action);
    }

    private boolean isOnWorldThread() {
        return world.isInThread();
    }

    private <T> T onWorldThread(Supplier<T> action) {
        if (isOnWorldThread()) return action.get();
        CompletableFuture<T> future = new CompletableFuture<>();
        world.execute(() -> {
            try { future.complete(action.get()); }
            catch (Throwable throwable) { future.completeExceptionally(throwable); }
        });
        try { return future.get(30, TimeUnit.SECONDS); }
        catch (java.util.concurrent.ExecutionException e) { throw new RuntimeException(e.getCause()); }
        catch (Exception e) { throw new RuntimeException("World thread dispatch failed", e); }
    }

    private void runOnWorldThread(Runnable action) {
        if (isOnWorldThread()) { action.run(); return; }
        CompletableFuture<Void> future = new CompletableFuture<>();
        world.execute(() -> {
            try { action.run(); future.complete(null); }
            catch (Throwable throwable) { future.completeExceptionally(throwable); }
        });
        try { future.get(30, TimeUnit.SECONDS); }
        catch (java.util.concurrent.ExecutionException e) { throw new RuntimeException(e.getCause()); }
        catch (Exception e) { throw new RuntimeException("World thread dispatch failed", e); }
    }


    @Override
    public World getWorld() { return world; }

    @Override
    public Store<EntityStore> getStore() { return store; }

    @Override
    public Object getCommandBuffer() { return commandBuffer; }

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
    public void setBlock(int x, int y, int z, String blockTypeId) {
        runOnWorldThread(() -> {
            try {
                Object chunk = getChunkAt(x, z);
                if (chunk == null) {
                    log("setBlock: chunk not loaded at (%d, %d)", x >> 4, z >> 4);
                    return;
                }
                for (var method : chunk.getClass().getMethods()) {
                    if ("setBlock".equals(method.getName()) && method.getParameterCount() == 4) {
                        Class<?>[] paramTypes = method.getParameterTypes();
                        if (paramTypes[3] == String.class) {
                            method.invoke(chunk, x, y, z, blockTypeId);
                            return;
                        }
                    }
                }
                for (var method : chunk.getClass().getMethods()) {
                    if ("setBlock".equals(method.getName()) && method.getParameterCount() == 4
                            && paramTypes(method)[3] == int.class) {
                        int blockIndex = BlockType.getAssetMap().getIndex(blockTypeId);
                        method.invoke(chunk, x, y, z, blockIndex);
                        return;
                    }
                }
                log("setBlock: no suitable setBlock method found on chunk accessor");
            } catch (Exception e) {
                throw new RuntimeException("setBlock failed: " + e.getMessage(), e);
            }
        });
    }

    private Object getChunkAt(int blockX, int blockZ) {
        try {
            long chunkKey = ((long)(blockX >> 4) << 32) | ((long)(blockZ >> 4) & 0xFFFFFFFFL);
            for (var method : world.getClass().getMethods()) {
                if ("getChunkIfLoaded".equals(method.getName()) && method.getParameterCount() == 1) {
                    return method.invoke(world, chunkKey);
                }
            }
            for (var method : world.getClass().getMethods()) {
                if ("loadChunkIfInMemory".equals(method.getName()) && method.getParameterCount() == 1) {
                    return method.invoke(world, chunkKey);
                }
            }
        } catch (Exception _) {}
        return null;
    }

    private static Class<?>[] paramTypes(java.lang.reflect.Method method) {
        return method.getParameterTypes();
    }

    @Override
    public String getBlock(int x, int y, int z) {
        return onWorldThread(() -> {
            try {
                Object chunk = getChunkAt(x, z);
                if (chunk == null) return null;
                for (var method : chunk.getClass().getMethods()) {
                    if ("getBlock".equals(method.getName()) && method.getParameterCount() == 3) {
                        Object result = method.invoke(chunk, x, y, z);
                        if (result instanceof Integer blockIndex) {
                            var assetMap = BlockType.getAssetMap();
                            for (var getId : assetMap.getClass().getMethods()) {
                                if ("getId".equals(getId.getName()) && getId.getParameterCount() == 1
                                        && getId.getParameterTypes()[0] == int.class) {
                                    Object id = getId.invoke(assetMap, blockIndex);
                                    return id != null ? id.toString() : String.valueOf(blockIndex);
                                }
                            }
                            return String.valueOf(blockIndex);
                        }
                        return result != null ? result.toString() : null;
                    }
                }
            } catch (Exception _) {}
            return null;
        });
    }

    @Override
    public void fillRegion(int x1, int y1, int z1, int x2, int y2, int z2, String blockTypeId) {
        runOnWorldThread(() -> {
            try {
                int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
                int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
                int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
                int count = 0;
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            Object chunk = getChunkAt(x, z);
                            if (chunk != null) {
                                for (var method : chunk.getClass().getMethods()) {
                                    if ("setBlock".equals(method.getName()) && method.getParameterCount() == 4
                                            && method.getParameterTypes()[3] == String.class) {
                                        method.invoke(chunk, x, y, z, blockTypeId);
                                        count++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                log("fillRegion: set %d blocks to %s", count, blockTypeId);
            } catch (Exception e) {
                throw new RuntimeException("fillRegion failed: " + e.getMessage(), e);
            }
        });
    }


    @Override
    public Ref<EntityStore> spawnEntity(String entityTypeId) {
        return spawnEntity(entityTypeId, 0, 64, 0);
    }

    @Override
    public Ref<EntityStore> spawnEntity(String entityTypeId, double x, double y, double z) {
        return onWorldThread(() -> {
            Ref<EntityStore> ref = trySpawnNPC(entityTypeId, null, x, y, z);
            if (ref == null) {
                ref = spawnEmptyEntity(x, y, z);
            }
            if (entityTracker != null) entityTracker.trackEntity(ref);
            return ref;
        });
    }

    @Override
    public Ref<EntityStore> spawnNPC(String role, double x, double y, double z) {
        return spawnNPC(role, null, x, y, z);
    }

    @Override
    public Ref<EntityStore> spawnNPC(String role, String variant, double x, double y, double z) {
        return onWorldThread(() -> {
            Ref<EntityStore> ref = trySpawnNPC(role, variant, x, y, z);
            if (ref == null) {
                throw new RuntimeException("Failed to spawn NPC with role '" + role + "'"
                        + (variant != null ? " variant '" + variant + "'" : "")
                        + ". NPCPlugin may be unavailable or the role is not recognized.");
            }
            if (entityTracker != null) entityTracker.trackEntity(ref);
            return ref;
        });
    }

    private Ref<EntityStore> trySpawnNPC(String role, String variant, double x, double y, double z) {
        try {
            Class<?> npcPluginClass = Class.forName("com.hypixel.hytale.server.npc.NPCPlugin");
            Object npcPlugin = npcPluginClass.getMethod("get").invoke(null);

            for (var method : npcPluginClass.getMethods()) {
                if ("spawnNPC".equals(method.getName()) && method.getParameterCount() == 5) {
                    Object result = method.invoke(npcPlugin, store, role, variant,
                            new com.hypixel.hytale.math.vector.Vector3d(x, y, z),
                            new com.hypixel.hytale.math.vector.Vector3f(0, 0, 0));
                    if (result != null) {
                        var firstMethod = result.getClass().getMethod("first");
                        @SuppressWarnings("unchecked")
                        Ref<EntityStore> ref = (Ref<EntityStore>) firstMethod.invoke(result);
                        if (ref != null) {
                            protectFromDespawn(ref);
                            return ref;
                        }
                    }
                }
            }
        } catch (Exception _) {}
        return null;
    }

    private Ref<EntityStore> spawnEmptyEntity(double x, double y, double z) {
        Ref<EntityStore> ref = store.addEntity(Archetype.empty(), AddReason.SPAWN);
        try {
            TransformComponent transform = new TransformComponent(
                    new com.hypixel.hytale.math.vector.Vector3d(x, y, z),
                    new com.hypixel.hytale.math.vector.Vector3f(0, 0, 0)
            );
            store.putComponent(ref, TransformComponent.getComponentType(), transform);
        } catch (Exception _) {}
        protectFromDespawn(ref);
        return ref;
    }

    private void protectFromDespawn(Ref<EntityStore> ref) {
        try {
            store.putComponent(ref, NewSpawnComponent.getComponentType(), new NewSpawnComponent(30.0f));
        } catch (Exception _) {}
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
    public boolean entityExists(Object entityRef) {
        if (entityRef == null) return false;
        return onWorldThread(() -> {
            try {
                @SuppressWarnings("unchecked")
                Ref<EntityStore> ref = (Ref<EntityStore>) entityRef;
                return ref.isValid();
            } catch (Exception _) {
                return false;
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void despawn(Object entityRef) {
        if (entityRef == null) return;
        runOnWorldThread(() -> store.removeEntity((Ref<EntityStore>) entityRef, RemoveReason.REMOVE));
    }


    @Override
    public void waitTicks(int ticks) {
        if (ticks <= 0) return;
        if (isOnWorldThread()) {
            throw new RuntimeException(
                    "waitTicks cannot be called from the world thread. "
                    + "Use @FlowTest or @AsyncTest instead of @WorldTest for tests that wait for ticks.");
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
        if (ticks <= 0) {
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
        if (isOnWorldThread()) {
            throw new RuntimeException(
                    "awaitCondition cannot be called from the world thread. "
                    + "Use @FlowTest or @AsyncTest instead of @WorldTest for tests that wait for ticks.");
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
                log("findEntities fallback - reflection failed: %s", e.getMessage());
            }
            return results;
        });
    }

    @Override
    public int countEntities(Object componentType) {
        return findEntities(componentType).size();
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
    @SuppressWarnings("unchecked")
    public double[] getPosition(Object entityRef) {
        return onWorldThread(() -> {
            TransformComponent transform = store.getComponent(
                    (Ref<EntityStore>) entityRef, TransformComponent.getComponentType());
            if (transform == null) return null;
            var pos = transform.getPosition();
            return new double[]{pos.getX(), pos.getY(), pos.getZ()};
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setPosition(Object entityRef, double x, double y, double z) {
        runOnWorldThread(() -> {
            TransformComponent transform = store.getComponent(
                    (Ref<EntityStore>) entityRef, TransformComponent.getComponentType());
            if (transform != null) {
                transform.setPosition(new com.hypixel.hytale.math.vector.Vector3d(x, y, z));
            }
        });
    }

    public void cleanup() {
        closeCaptures();
    }
}
