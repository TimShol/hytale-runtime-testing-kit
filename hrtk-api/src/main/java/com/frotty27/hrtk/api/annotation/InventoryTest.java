package com.frotty27.hrtk.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation that combines {@link HytaleTest} and {@code @Tag("inventory")}
 * into a single declaration for inventory system tests.
 *
 * <p>Methods annotated with {@code @InventoryTest} indicate that the test exercises
 * inventory operations such as adding, removing, moving, and inspecting item stacks.
 * A world context is available for tests that need to interact with container blocks
 * or entity inventories in-world.</p>
 *
 * <pre>{@code
 * @InventoryTest
 * void testAddItem() {
 *     Inventory inv = Inventory.create(36);
 *     inv.addItem(new ItemStack(Items.STONE, 64));
 *     HytaleAssert.assertEquals(64, inv.getSlot(0).getCount());
 * }
 *
 * @InventoryTest(world = "storage_room")
 * void testChestContents(WorldTestContext ctx) {
 *     // Test container block inventory in a specific world
 * }
 * }</pre>
 *
 * @see HytaleTest
 * @see WorldTest
 * @see CombatTest
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InventoryTest {

    /**
     * Name of the world to use. When empty, the default test world is selected.
     *
     * @return the world name, or empty string for the default test world
     */
    String world() default "";
}
