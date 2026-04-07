package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.assert_.InventoryAssert;
import com.frotty27.hrtk.api.assert_.ItemAssert;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;

@HytaleSuite(value = "Item and Inventory Tests", isolation = IsolationStrategy.NONE)
@Tag({"items", "inventory"})
public class ItemInventoryTests {

    @HytaleTest
    @DisplayName("ItemStack stores item ID and quantity")
    @Order(1)
    void testItemStackCreation() {
        var sword = new ItemStack("Weapon_Sword_Iron", 1);

        ItemAssert.assertItemId(sword, "Weapon_Sword_Iron");
        ItemAssert.assertItemQuantity(sword, 1);
        ItemAssert.assertItemNotEmpty(sword);
    }

    @HytaleTest
    @DisplayName("Empty item stack is detected correctly")
    @Order(2)
    void testEmptyItemStack() {
        ItemAssert.assertItemEmpty(ItemStack.EMPTY);
        HytaleAssert.assertTrue(
            "EMPTY constant should report isEmpty",
            ItemStack.EMPTY.isEmpty()
        );
    }

    @HytaleTest
    @DisplayName("withDurability returns a new stack with reduced durability")
    @Order(3)
    void testDurabilityReduction() {
        var pickaxe = new ItemStack("Tool_Pickaxe_Stone", 1);
        double maxDur = pickaxe.getMaxDurability();

        HytaleAssert.assertTrue(
            "Tool should have max durability > 0",
            maxDur > 0
        );
        ItemAssert.assertItemNotBroken(pickaxe);

        var damaged = pickaxe.withDurability(1.0);
        HytaleAssert.assertEquals(1.0, damaged.getDurability(), 0.01);

        var broken = pickaxe.withDurability(0.0);
        ItemAssert.assertItemBroken(broken);
    }

    @HytaleTest
    @DisplayName("Same items are stackable, different items are not")
    @Order(4)
    void testStackability() {
        var oakLog1 = new ItemStack("Wood_Oak_Trunk", 10);
        var oakLog2 = new ItemStack("Wood_Oak_Trunk", 5);
        var birchLog = new ItemStack("Wood_Birch_Trunk", 10);

        ItemAssert.assertItemStackable(oakLog1, oakLog2);
        HytaleAssert.assertTrue(
            "Same item type should be stackable",
            oakLog1.isStackableWith(oakLog2)
        );
        HytaleAssert.assertFalse(
            "Different items should not be stackable",
            oakLog1.isStackableWith(birchLog)
        );
    }

    @HytaleTest
    @DisplayName("withQuantity returns new stack, does not mutate original")
    @Order(5)
    void testWithQuantity() {
        var arrows = new ItemStack("Ammo_Arrow", 64);
        var half = arrows.withQuantity(32);

        ItemAssert.assertItemQuantity(arrows, 64);
        ItemAssert.assertItemQuantity(half, 32);

        HytaleAssert.assertTrue(
            "Same type should be equivalent",
            arrows.isEquivalentType(half)
        );
    }

    @HytaleTest
    @DisplayName("SimpleItemContainer add, get, and remove")
    @Order(6)
    void testSimpleContainer() {
        var container = new SimpleItemContainer((short) 10);

        HytaleAssert.assertEquals(10, (int) container.getCapacity());
        HytaleAssert.assertTrue("New container should be empty", container.isEmpty());

        var sword = new ItemStack("Weapon_Sword_Iron", 1);
        container.addItemStackToSlot((short) 0, sword);

        HytaleAssert.assertFalse("Container should not be empty after add", container.isEmpty());

        var retrieved = container.getItemStack((short) 0);
        ItemAssert.assertItemId(retrieved, "Weapon_Sword_Iron");
        ItemAssert.assertItemQuantity(retrieved, 1);

        container.removeItemStackFromSlot((short) 0);
        HytaleAssert.assertTrue(
            "Container should be empty after removing only item",
            container.isEmpty()
        );
    }

    @HytaleTest
    @DisplayName("Inventory sections are separate")
    @Order(7)
    void testInventorySections() {
        var inventory = new Inventory();

        HytaleAssert.assertNotNull("Storage section", inventory.getStorage());
        HytaleAssert.assertNotNull("Hotbar section", inventory.getHotbar());
        HytaleAssert.assertNotNull("Armor section", inventory.getArmor());
        HytaleAssert.assertNotNull("Utility section", inventory.getUtility());
        HytaleAssert.assertNotNull("Tools section", inventory.getTools());

        inventory.clear();

        InventoryAssert.assertInventoryEmpty(inventory);
    }

    @HytaleTest
    @DisplayName("Container rejects items when full")
    @Order(8)
    void testContainerCapacityCheck() {
        var container = new SimpleItemContainer((short) 1);
        var sword = new ItemStack("Weapon_Sword_Iron", 1);

        container.addItemStackToSlot((short) 0, sword);
        HytaleAssert.assertFalse("Full container should not be empty", container.isEmpty());

        HytaleAssert.assertFalse(
            "Full container should not accept more items",
            container.canAddItemStack(new ItemStack("Ammo_Arrow", 1))
        );
    }

    @HytaleTest
    @DisplayName("countItemStacks returns correct count after adds and removes")
    @Order(9)
    void testCountItemStacks() {
        var container = new SimpleItemContainer((short) 10);

        container.addItemStackToSlot((short) 0, new ItemStack("Weapon_Sword_Iron", 1));
        container.addItemStackToSlot((short) 1, new ItemStack("Wood_Oak_Trunk", 5));
        container.addItemStackToSlot((short) 2, new ItemStack("Ammo_Arrow", 32));

        HytaleAssert.assertEquals(3, container.countItemStacks(stack -> !stack.isEmpty()));

        container.removeItemStackFromSlot((short) 1);

        HytaleAssert.assertEquals(2, container.countItemStacks(stack -> !stack.isEmpty()));
    }
}
