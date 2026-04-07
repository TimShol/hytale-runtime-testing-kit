package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.annotation.WorldTest;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.assert_.WorldAssert;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

@HytaleSuite(value = "Block and World Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag({"blocks", "world"})
public class BlockWorldTests {

    @WorldTest
    @DisplayName("Block placed at coordinates can be read back")
    @Order(1)
    void testBlockPlacementAndRead(WorldTestContext ctx) {
        ctx.setBlock(10, 64, 10, "Rock_Stone");

        WorldAssert.assertBlockAt(ctx.getWorld(), 10, 64, 10, "Rock_Stone");
    }

    @HytaleTest
    @DisplayName("BlockType.fromString returns valid block with properties")
    @Order(2)
    void testBlockTypeFromString() {
        var stone = BlockType.fromString("Rock_Stone");
        HytaleAssert.assertNotNull("BlockType should resolve from string", stone);

        HytaleAssert.assertNotNull("Block should have an ID", stone.getId());
        HytaleAssert.assertNotNull("Block should have a material", stone.getMaterial());

        HytaleAssert.assertFalse(
            "Stone should not be a trigger block",
            stone.isTrigger()
        );
    }

    @WorldTest
    @DisplayName("fillRegion fills all blocks in the specified range")
    @Order(3)
    void testFillRegion(WorldTestContext ctx) {
        ctx.fillRegion(0, 60, 0, 4, 60, 4, "Rock_Stone");

        WorldAssert.assertBlockAt(ctx.getWorld(), 0, 60, 0, "Rock_Stone");
        WorldAssert.assertBlockAt(ctx.getWorld(), 4, 60, 4, "Rock_Stone");
        WorldAssert.assertBlockAt(ctx.getWorld(), 2, 60, 2, "Rock_Stone");
    }

    @HytaleTest
    @DisplayName("Certain block types have entity damage values")
    @Order(4)
    void testBlockDamageToEntities() {
        var stone = BlockType.fromString("Rock_Stone");
        HytaleAssert.assertNotNull(stone);

        int damage = stone.getDamageToEntities();
        HytaleAssert.assertTrue(
            "Stone should not deal damage to entities",
            damage == 0
        );
    }

    @WorldTest
    @DisplayName("WorldConfig is accessible and has valid values")
    @Order(5)
    void testWorldConfig(WorldTestContext ctx) {
        var world = ctx.getWorld();
        HytaleAssert.assertNotNull(world);

        var config = ((World) world).getWorldConfig();
        HytaleAssert.assertNotNull("WorldConfig should not be null", config);
    }

    @WorldTest
    @DisplayName("Universe and world listing includes the test world")
    @Order(6)
    void testUniverseAndWorldListing(WorldTestContext ctx) {
        var universe = Universe.get();
        HytaleAssert.assertNotNull("Universe.get() should not be null", universe);

        var worlds = universe.getWorlds();
        HytaleAssert.assertNotNull("Worlds map should not be null", worlds);
        HytaleAssert.assertFalse("Worlds map should not be empty", worlds.isEmpty());
    }

    @WorldTest
    @DisplayName("World tick counter increases over time")
    @Order(7)
    void testWorldTickCounter(WorldTestContext ctx) {
        var world = ctx.getWorld();
        HytaleAssert.assertNotNull(world);

        long tick1 = ((World) world).getTick();
        HytaleAssert.assertTrue(
            "World tick should be non-negative",
            tick1 >= 0
        );
    }
}
