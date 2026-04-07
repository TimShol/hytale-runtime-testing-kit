package com.frotty27.hrtk.api.context;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Test context for ECS-level tests, providing direct Store and CommandBuffer access,
 * entity creation, component operations, tick-waiting, and entity queries.
 *
 * <p>Available when using the {@code @EcsTest} annotation. Declare an
 * {@code EcsTestContext} parameter in your test method to receive this context.</p>
 *
 * <pre>{@code
 * @EcsTest
 * public void testComponentAttachment(EcsTestContext ctx) {
 *     Object entity = ctx.createEntity();
 *     ctx.putComponent(entity, myComponentType, new MyComponent());
 *     ctx.flush();
 *     assertTrue(ctx.hasComponent(entity, myComponentType));
 *     ctx.waitTicks(5);
 * }
 * }</pre>
 *
 * @see TestContext
 * @see WorldTestContext
 * @since 1.0.0
 */
public interface EcsTestContext extends TestContext {

    /**
     * Returns the ECS store for the test environment.
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
     * Creates a new empty entity in the ECS store.
     *
     * @return an entity reference (runtime type: {@code Ref<EntityStore>})
     */
    Object createEntity();

    /**
     * Flushes the command buffer, executing all deferred operations immediately.
     */
    void flush();

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
     * Retrieves a component from an entity.
     *
     * @param entityRef     the entity reference (runtime type: {@code Ref<EntityStore>})
     * @param componentType the component type to retrieve (runtime type: {@code ComponentType<EntityStore, T>})
     * @return the component instance, or {@code null} if the entity does not have it
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
     * Blocks the test thread until the specified number of world ticks have elapsed.
     *
     * @param ticks the number of world ticks to wait
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
     * @param maxTicks  the maximum number of ticks to wait before timing out
     * @param <T>       the result type
     * @return the first non-null result from the condition supplier
     * @throws com.frotty27.hrtk.api.assert_.AssertionFailedException if maxTicks elapse without success
     */
    <T> T awaitCondition(Supplier<T> condition, int maxTicks);

    /**
     * Polls a condition each tick with a custom failure message on timeout.
     *
     * @param condition   a supplier that returns non-null when the condition is met
     * @param maxTicks    the maximum number of ticks to wait before timing out
     * @param failMessage the message to include in the timeout exception
     * @param <T>         the result type
     * @return the first non-null result from the condition supplier
     * @throws com.frotty27.hrtk.api.assert_.AssertionFailedException if maxTicks elapse without success
     */
    <T> T awaitCondition(Supplier<T> condition, int maxTicks, String failMessage);

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
}
