package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation that combines {@link HytaleTest}, {@code @Tag("flow")},
 * world context, and an asynchronous timeout into a single declaration.
 *
 * <p>Methods annotated with {@code @FlowTest} represent multi-step flow tests that
 * may span several server ticks. The test runner provides world context and waits up
 * to {@link #timeoutTicks()} ticks for the test to complete. This is ideal for testing
 * sequences of game actions that unfold over time, such as quest progression, NPC
 * dialogues, or scripted encounters.</p>
 *
 * <pre>{@code
 * @FlowTest
 * void testQuestProgression(WorldTestContext ctx, AsyncTestContext async) {
 *     QuestManager.startQuest(player, "tutorial");
 *     QuestManager.onObjectiveComplete(player, "gather_wood", () -> {
 *         HytaleAssert.assertEquals("tutorial_step_2", player.getCurrentObjective());
 *         async.complete();
 *     });
 * }
 *
 * @FlowTest(world = "dungeon_01", timeoutTicks = 400)
 * void testDungeonClear(WorldTestContext ctx, AsyncTestContext async) {
 *     // Multi-tick dungeon encounter test
 * }
 * }</pre>
 *
 * @see HytaleTest
 * @see AsyncTest
 * @see WorldTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FlowTest {

    /**
     * Name of the world to use. When empty, the default test world is selected.
     *
     * @return the world name, or empty string for the default test world
     */
    String world() default "";

    /**
     * Maximum number of server ticks to wait before the test is considered timed out.
     *
     * @return the timeout in server ticks (defaults to 200)
     */
    int timeoutTicks() default 200;
}
