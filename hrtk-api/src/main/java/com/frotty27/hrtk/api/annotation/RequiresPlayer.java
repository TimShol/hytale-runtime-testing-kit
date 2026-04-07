package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a test method requires one or more mock players.
 *
 * <p>The test runner will create the specified number of mock players before
 * the test executes and enable {@code MockCommandSender} injection. Mock players
 * are automatically cleaned up after the test completes.</p>
 *
 * <pre>{@code
 * @HytaleTest
 * @RequiresPlayer
 * void testSinglePlayer(MockCommandSender player) {
 *     HytaleAssert.assertNotNull(player);
 * }
 *
 * @HytaleTest
 * @RequiresPlayer(count = 4)
 * void testMultiplayer() {
 *     HytaleAssert.assertEquals(4, TestPlayers.count());
 * }
 * }</pre>
 *
 * @see RequiresWorld
 * @see RequiresPlugin
 * @see HytaleTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequiresPlayer {

    /**
     * The number of mock players to create for the test.
     *
     * @return the player count (defaults to 1)
     */
    int count() default 1;
}
