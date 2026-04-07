package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.assert_.LootAssert;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;

import java.util.ArrayList;

@HytaleSuite(value = "Loot Tests", isolation = IsolationStrategy.NONE)
@Tag("loot")
public class LootTests {

    @HytaleTest
    @DisplayName("Trork Warrior drops fabric scraps")
    @Order(1)
    void trorkWarriorDropsFabric() {
        var dropList = ItemDropList.getAssetMap().getAsset("Drop_Trork_Warrior");
        HytaleAssert.assertNotNull("Drop_Trork_Warrior should exist", dropList);

        var drops = new ArrayList<ItemDrop>();
        dropList.getContainer().getAllDrops(drops);
        HytaleAssert.assertFalse("Trork Warrior should have drops", drops.isEmpty());

        LootAssert.assertDropsContain(drops, "Ingredient_Fabric_Scrap_Linen");
    }

    @HytaleTest
    @DisplayName("Emberwulf drops hide and fire essence")
    @Order(2)
    void emberwulfDropsHideAndEssence() {
        var dropList = ItemDropList.getAssetMap().getAsset("Drop_Emberwulf");
        HytaleAssert.assertNotNull("Drop_Emberwulf should exist", dropList);

        var drops = new ArrayList<ItemDrop>();
        dropList.getContainer().getAllDrops(drops);
        HytaleAssert.assertFalse("Emberwulf should have drops", drops.isEmpty());

        LootAssert.assertDropsContain(drops, "Ingredient_Hide_Heavy");
        LootAssert.assertDropsContain(drops, "Ingredient_Fire_Essence");
    }

    @HytaleTest
    @DisplayName("Empty drop list produces no items")
    @Order(3)
    void emptyDropListHasNothing() {
        var dropList = ItemDropList.getAssetMap().getAsset("Empty");
        if (dropList != null) {
            var drops = new ArrayList<ItemDrop>();
            dropList.getContainer().getAllDrops(drops);
            LootAssert.assertNoDrops(drops);
        }
    }
}
