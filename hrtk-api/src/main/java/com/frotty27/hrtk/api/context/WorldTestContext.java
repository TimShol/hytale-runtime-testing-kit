package com.frotty27.hrtk.api.context;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Test context for world-level tests, providing access to the test world,
 * ECS store, block operations, entity spawning, tick-waiting, and entity queries.
 *
 * <p>Available when using {@code @RequiresWorld}, {@code @WorldTest},
 * {@code @CombatTest}, {@code @SpawnTest}, or {@code @FlowTest} annotations.
 * Declare a {@code WorldTestContext} parameter in your test method to receive
 * this context.</p>
 *
 * <pre>{@code
 * @WorldTest
 * public void testBlockPlacement(WorldTestContext ctx) {
 *     ctx.setBlock(10, 64, 10, "Rock_Stone");
 *     ctx.waitTicks(1);
 *     assertEquals("Rock_Stone", ctx.getBlock(10, 64, 10));
 *
 *     Object mob = ctx.spawnEntity("Kweebec_Sapling", 10.0, 65.0, 10.0);
 *     ctx.waitTicks(10);
 *     assertTrue(ctx.entityExists(mob));
 * }
 * }</pre>
 *
 * @see TestContext
 * @see EcsTestContext
 * @since 1.0.0
 */
public interface WorldTestContext extends TestContext {

    /**
     * Returns the test world instance.
     *
     * @return the Hytale World object (runtime type: {@code World})
     */
    Object getWorld();

    /**
     * Returns the entity store for the test world.
     *
     * @return the store object (runtime type: {@code Store<EntityStore>})
     */
    Object getStore();

    /**
     * Returns a command buffer for batching deferred ECS operations.
     *
     * <p>Operations queued through the command buffer are applied when
     * {@link #flush()} is called.</p>
     *
     * @return the command buffer object
     */
    Object getCommandBuffer();

    /**
     * Flushes the command buffer, executing all deferred operations immediately.
     */
    void flush();

    /**
     * Sets a block at the given coordinates in the test world.
     *
     * @param x           the block X coordinate
     * @param y           the block Y coordinate
     * @param z           the block Z coordinate
     * @param blockTypeId the block type identifier (e.g., {@code "Rock_Stone"})
     */
    void setBlock(int x, int y, int z, String blockTypeId);

    /**
     * Returns the block type ID at the given coordinates in the test world.
     *
     * @param x the block X coordinate
     * @param y the block Y coordinate
     * @param z the block Z coordinate
     * @return the block type identifier at the coordinates
     */
    String getBlock(int x, int y, int z);

    /**
     * Fills a rectangular region with a block type.
     *
     * <p>The region is defined by two opposite corners {@code (x1,y1,z1)} and
     * {@code (x2,y2,z2)}, inclusive.</p>
     *
     * @param x1          the first corner X coordinate
     * @param y1          the first corner Y coordinate
     * @param z1          the first corner Z coordinate
     * @param x2          the second corner X coordinate
     * @param y2          the second corner Y coordinate
     * @param z2          the second corner Z coordinate
     * @param blockTypeId the block type identifier to fill with
     */
    void fillRegion(int x1, int y1, int z1, int x2, int y2, int z2, String blockTypeId);

    /**
     * Spawns an entity at the world's default spawn position.
     *
     * <p>Attempts to spawn a typed NPC using the given role name via NPCPlugin.
     * Falls back to an empty entity with a TransformComponent if NPCPlugin
     * is unavailable or the role is not recognized.</p>
     *
     * @param entityTypeId the NPC role name (e.g., {@code "Trork_Warrior"}, {@code "Kweebec_Sapling"})
     * @return an entity reference (runtime type: {@code Ref<EntityStore>})
     */
    Object spawnEntity(String entityTypeId);

    /**
     * Spawns an entity at the specified coordinates.
     *
     * <p>Attempts to spawn a typed NPC using the given role name via NPCPlugin.
     * Falls back to an empty entity with a TransformComponent if NPCPlugin
     * is unavailable or the role is not recognized.</p>
     *
     * @param entityTypeId the NPC role name (e.g., {@code "Trork_Warrior"}, {@code "Kweebec_Sapling"})
     * @param x            the spawn X coordinate
     * @param y            the spawn Y coordinate
     * @param z            the spawn Z coordinate
     * @return an entity reference (runtime type: {@code Ref<EntityStore>})
     */
    Object spawnEntity(String entityTypeId, double x, double y, double z);

    /**
     * Spawns an NPC with the given role at the specified coordinates.
     *
     * <p>Uses NPCPlugin to spawn a fully typed NPC entity. Unlike
     * {@link #spawnEntity(String, double, double, double)}, this method does
     * not fall back to an empty entity - it throws if NPCPlugin is unavailable
     * or the role is not recognized.</p>
     *
     * @param role the NPC role name (e.g., {@code "Trork_Warrior"}, {@code "Kweebec_Sapling"})
     * @param x    the spawn X coordinate
     * @param y    the spawn Y coordinate
     * @param z    the spawn Z coordinate
     * @return an entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws RuntimeException if NPCPlugin is unavailable or the role is invalid
     */
    Object spawnNPC(String role, double x, double y, double z);

    /**
     * Spawns an NPC with the given role and variant at the specified coordinates.
     *
     * <p>Uses NPCPlugin to spawn a fully typed NPC entity with a specific
     * variant. This gives full control over the NPC that is created.</p>
     *
     * <pre>{@code
     * @WorldTest
     * void testTrorkWarrior(WorldTestContext ctx) {
     *     Object warrior = ctx.spawnNPC("Trork_Warrior", null, 10, 64, 10);
     *     HytaleAssert.assertNotNull("Warrior ref", warrior);
     *     HytaleAssert.assertTrue(ctx.entityExists(warrior));
     * }
     * }</pre>
     *
     * @param role    the NPC role name (e.g., {@code "Trork_Warrior"})
     * @param variant the NPC variant, or {@code null} for the default variant
     * @param x       the spawn X coordinate
     * @param y       the spawn Y coordinate
     * @param z       the spawn Z coordinate
     * @return an entity reference (runtime type: {@code Ref<EntityStore>})
     * @throws RuntimeException if NPCPlugin is unavailable or the role is invalid
     */
    Object spawnNPC(String role, String variant, double x, double y, double z);

    /**
     * Attaches a component to an entity via the command buffer.
     *
     * <p>Call {@link #flush()} to apply the operation.</p>
     *
     * @param entityRef     the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param componentType the component type (runtime type: {@code ComponentType<EntityStore, T>})
     * @param component     the component instance to attach
     */
    void putComponent(Object entityRef, Object componentType, Object component);

    /**
     * Removes a component from an entity via the command buffer.
     *
     * <p>Call {@link #flush()} to apply the operation.</p>
     *
     * @param entityRef     the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param componentType the component type to remove (runtime type: {@code ComponentType<EntityStore, T>})
     */
    void removeComponent(Object entityRef, Object componentType);

    /**
     * Checks whether an entity reference is still valid in the world.
     *
     * @param entityRef the entity reference to check
     * @return {@code true} if the entity still exists, {@code false} otherwise
     */
    boolean entityExists(Object entityRef);

    /**
     * Removes an entity from the world.
     *
     * @param entityRef the entity reference to despawn
     */
    void despawn(Object entityRef);

    /**
     * Blocks the test thread until the specified number of world ticks have elapsed.
     *
     * <p>This method requires the test to run off the world thread. Use
     * {@code @FlowTest} or {@code @AsyncTest} for tests that call this method.
     * Calling from the world thread (e.g., inside a {@code @WorldTest}) throws
     * a {@code RuntimeException} with a descriptive message.</p>
     *
     * <p>Entities spawned via {@code spawnEntity} with a fallback empty entity
     * may not survive multiple ticks, as the world's systems can garbage-collect
     * componentless entities. Use {@code spawnNPC} with a valid role name for
     * flow tests that need entities to persist across ticks.</p>
     *
     * @param ticks the number of world ticks to wait
     * @throws RuntimeException if called from the world thread
     */
    void waitTicks(int ticks);

    /**
     * Waits for the specified number of ticks asynchronously.
     *
     * @param ticks the number of world ticks to wait
     * @return a future that completes after the ticks have elapsed
     */
    CompletableFuture<Void> waitTicksAsync(int ticks);

    /**
     * Polls a condition each tick until it returns a non-null value, or times out.
     *
     * <p>The supplier is invoked once per tick. If it returns a non-null value,
     * that value is returned immediately. If {@code maxTicks} elapse without a
     * non-null result, the assertion fails.</p>
     *
     * @param condition a supplier that returns non-null when the condition is met
     * @param maxTicks  the maximum number of ticks to wait before throwing
     * @param <T>       the result type
     * @return the first non-null result from the condition supplier
     * @throws RuntimeException if maxTicks elapse without the condition being met
     */
    <T> T awaitCondition(Supplier<T> condition, int maxTicks);

    /**
     * Polls a condition each tick with a custom failure message on timeout.
     *
     * @param condition   a supplier that returns non-null when the condition is met
     * @param maxTicks    the maximum number of ticks to wait
     * @param failMessage the message to include in the timeout exception
     * @param <T>         the result type
     * @return the first non-null result from the condition supplier
     * @throws RuntimeException if maxTicks elapse without the condition being met
     */
    <T> T awaitCondition(Supplier<T> condition, int maxTicks, String failMessage);

    /**
     * Executes an action on the world thread and returns the result.
     *
     * <p>Use this when calling mod APIs that internally access the Store or
     * CommandBuffer and require world thread affinity. If the test is already
     * running on the world thread, the action executes directly. If running
     * on a different thread (e.g., inside a {@code @FlowTest}), the action
     * is dispatched via {@code world.execute()} and the calling thread blocks
     * until it completes.</p>
     *
     * @param action the action to execute on the world thread
     * @param <T>    the return type
     * @return the result of the action
     */
    <T> T executeOnWorld(java.util.function.Supplier<T> action);

    /**
     * Runs an action on the world thread without returning a result.
     *
     * @param action the action to run on the world thread
     * @see #executeOnWorld(java.util.function.Supplier)
     */
    void runOnWorld(Runnable action);

    /**
     * Finds all entity references that have the given component type.
     *
     * @param componentType the component type to query (runtime type: {@code ComponentType<EntityStore, T>})
     * @return a list of matching entity references, possibly empty
     */
    List<?> findEntities(Object componentType);

    /**
     * Counts the number of entities that have the given component type.
     *
     * @param componentType the component type to query (runtime type: {@code ComponentType<EntityStore, T>})
     * @return the number of entities matching the query
     */
    int countEntities(Object componentType);

    /**
     * Retrieves a component from an entity.
     *
     * @param entityRef     the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param componentType the component type to retrieve (runtime type: {@code ComponentType<EntityStore, T>})
     * @return the component instance, or {@code null} if absent
     */
    Object getComponent(Object entityRef, Object componentType);

    /**
     * Checks whether an entity has a specific component attached.
     *
     * @param entityRef     the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param componentType the component type to check (runtime type: {@code ComponentType<EntityStore, T>})
     * @return {@code true} if the entity has the component, {@code false} otherwise
     */
    boolean hasComponent(Object entityRef, Object componentType);

    /**
     * Returns the position of an entity as a double array.
     *
     * <p>The position is derived from the entity's transform component. Returns
     * {@code null} if the entity has no transform.</p>
     *
     * @param entityRef the entity reference (runtime type: {@code Ref<EntityStore>})
     * @return a three-element array {@code [x, y, z]}, or {@code null} if no transform exists
     */
    double[] getPosition(Object entityRef);

    /**
     * Sets the position of an entity in the world.
     *
     * @param entityRef the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param x         the X coordinate
     * @param y         the Y coordinate
     * @param z         the Z coordinate
     */
    void setPosition(Object entityRef, double x, double y, double z);
}
