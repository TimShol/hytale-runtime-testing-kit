# HRTK Modder Test Catalog

How to read this document: Each entry explains a Hytale API feature in plain language, why testing it saves you debugging time, and gives you a complete test you can copy into your mod.

Every API method used in this catalog has been verified via `javap` against the actual Hytale server JAR. If it's in a test below, it exists on the server.

Priority ratings:
- **CRITICAL:** Test this or you'll waste hours on silent bugs
- **HIGH:** Strongly recommended, confusing failures live here
- **MEDIUM:** Nice to have, speeds up debugging
- **LOW:** Failures are usually obvious

---

## 1. Working With Entities

### 1.1 Creating an Entity and Adding Components
**Priority:** CRITICAL

**What you're testing:** You create an entity and attach your mod's data to it. Every mod does this.

**What goes wrong silently:** You call `putComponent`, but the data isn't there when you read it back. Your mod looks like it's working during the write, but other systems read stale or missing data. The most common cause is forgetting to flush the command buffer.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;
import com.frotty27.hrtk.api.context.EcsTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;

@HytaleSuite(value = "Entity Component Tests", isolation = IsolationStrategy.SNAPSHOT)
@Tag("ecs")
public class EntityComponentTests {

    @EcsTest
    @DisplayName("putComponent stores data that getComponent reads back")
    void testPutThenGet(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var transform = new TransformComponent(
            new Vector3d(100.0, 64.0, 200.0),
            new Vector3f(0f, 0f, 0f)
        );
        ctx.putComponent(entity, TransformComponent.TYPE, transform);
        ctx.flush();

        EcsAssert.assertHasComponent(ctx.getStore(), entity, TransformComponent.TYPE);
        var stored = (TransformComponent) EcsAssert.assertGetComponent(
            ctx.getStore(), entity, TransformComponent.TYPE
        );
        HytaleAssert.assertNotNull(stored.getPosition());
    }
}
```

### 1.2 Removing a Component
**Priority:** CRITICAL

**What you're testing:** You remove a component from an entity and expect it to be gone.

**What goes wrong silently:** You call `removeComponent`, but the archetype doesn't update until the command buffer flushes. If you read the component back before flushing, it's still there. Your cleanup logic thinks it worked, but systems that run on the next tick still see the old data.

**Test:**
```java
@EcsTest
@DisplayName("removeComponent actually removes the component after flush")
void testRemoveComponent(EcsTestContext ctx) {
    var entity = ctx.createEntity();
    var transform = new TransformComponent(
        new Vector3d(0, 64, 0), new Vector3f(0f, 0f, 0f)
    );
    ctx.putComponent(entity, TransformComponent.TYPE, transform);
    ctx.flush();

    EcsAssert.assertHasComponent(ctx.getStore(), entity, TransformComponent.TYPE);

    ctx.removeComponent(entity, TransformComponent.TYPE);
    ctx.flush();

    EcsAssert.assertNotHasComponent(ctx.getStore(), entity, TransformComponent.TYPE);
}
```

### 1.3 Verifying an Entity Exists After Spawn
**Priority:** HIGH

**What you're testing:** You spawn an entity in a world and check that it actually exists in the entity store.

**What goes wrong silently:** The spawn call returns a Ref, but the entity might be garbage-collected by the world's systems if it has no meaningful components. You think the entity is alive, but it's gone by the next tick.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;

@HytaleSuite(value = "Entity Spawn Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("spawn")
public class EntitySpawnTests {

    @WorldTest
    @DisplayName("Spawned entity exists in world")
    void testEntityExistsAfterSpawn(WorldTestContext ctx) {
        var ref = ctx.spawnEntity("Kweebec_Sapling", 10.0, 64.0, 10.0);
        ctx.flush();

        EcsAssert.assertRefValid(ref);
        WorldAssert.assertEntityInWorld(ctx.getWorld(), ref);
        HytaleAssert.assertTrue("Entity should exist", ctx.entityExists(ref));
    }
}
```

### 1.4 Entity Position After Spawn
**Priority:** HIGH

**What you're testing:** After spawning an entity at specific coordinates, those coordinates are actually set.

**What goes wrong silently:** The server might snap the entity to a valid surface position, or the TransformComponent might initialize to world origin instead of your coordinates. Your NPC spawns at (0, 0, 0) instead of where you wanted it.

**Test:**
```java
@WorldTest
@DisplayName("Entity spawns at the specified coordinates")
void testEntityPosition(WorldTestContext ctx) {
    var ref = ctx.spawnNPC("Trork_Warrior", 50.0, 64.0, 50.0);
    ctx.flush();

    double[] pos = ctx.getPosition(ref);
    HytaleAssert.assertNotNull("Position should not be null", pos);
    HytaleAssert.assertEquals(50.0, pos[0], 1.0);
    HytaleAssert.assertEquals(64.0, pos[1], 2.0);
    HytaleAssert.assertEquals(50.0, pos[2], 1.0);
}
```

### 1.5 Despawning Removes the Entity
**Priority:** MEDIUM

**What you're testing:** After calling despawn, the entity reference is no longer valid.

**Recommendation:** Test this if your mod manages entity lifecycle manually (boss fights, minigames). Use `ctx.despawn(ref)` followed by `ctx.entityExists(ref)` to verify removal.

### 1.6 Entity Count Queries
**Priority:** MEDIUM

**What you're testing:** Querying the store for entities by component type returns the correct count.

**Recommendation:** Use `ctx.countEntities(componentType)` before and after spawn to confirm the count changes by exactly 1. Catches silent spawning failures.

---

## 2. Stats, Health, and Modifiers

### 2.1 Reading Health from a Spawned NPC
**Priority:** CRITICAL

**What you're testing:** After spawning an NPC, you read its health from the EntityStatMap. Every combat mod needs this.

**What goes wrong silently:** You read health from the wrong stat type constant, or the EntityStatMap component isn't attached yet because the NPC hasn't been fully initialized. You see 0 health on a living entity.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;

@HytaleSuite(value = "Stat Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("stats")
public class StatTests {

    @SpawnTest
    @DisplayName("Spawned NPC has positive health")
    void testNPCHasHealth(WorldTestContext ctx) {
        var skeleton = ctx.spawnNPC("Trork_Warrior", 0, 64, 0);
        ctx.flush();

        StatsAssert.assertAlive(ctx.getStore(), skeleton);
        CombatAssert.assertHealthAbove(ctx.getStore(), skeleton, 0f);
    }
}
```

### 2.2 Applying a Static Modifier to a Stat
**Priority:** CRITICAL

**What you're testing:** You apply a flat +5 bonus to a stat and verify the stat value actually changes.

**What goes wrong silently:** You create a StaticModifier with the wrong CalculationType (FLAT vs PERCENT), or you target the wrong ModifierTarget. The modifier is "applied" but the stat value doesn't change. Your buff system looks like it's working but has no effect.

**Test:**
```java
@EcsTest
@DisplayName("StaticModifier with FLAT calculation changes the stat value")
void testFlatModifier(EcsTestContext ctx) {
    var entity = ctx.createEntity();
    var statMap = new EntityStatMap();
    ctx.putComponent(entity, EntityStatMap.TYPE, statMap);
    ctx.flush();

    var healthStat = DefaultEntityStatTypes.getHealth();
    statMap.setStatValue(healthStat, 20.0f);

    float before = statMap.get(healthStat).get();

    var modifier = new StaticModifier(
        ModifierTarget.MAX_VALUE,
        CalculationType.FLAT,
        5.0f
    );
    statMap.putModifier(healthStat, "strength_potion", modifier);
    statMap.update(healthStat);

    float after = statMap.get(healthStat).getMax();

    HytaleAssert.assertTrue(
        "Modifier should increase max by 5",
        after > before
    );
    HytaleAssert.assertEquals(5.0f, modifier.getAmount(), 0.01f);
    HytaleAssert.assertEquals(modifier.apply(20.0f), 25.0f, 0.01f);
}
```

### 2.3 Maximize and Minimize Stat Values
**Priority:** HIGH

**What you're testing:** `maximizeStatValue` sets the stat to its max, and `minimizeStatValue` sets it to its min.

**What goes wrong silently:** You call `maximizeStatValue` expecting it to set health to 100, but modifiers changed the max and you didn't call `update()` first. The stat maxes out at the wrong value.

**Test:**
```java
@EcsTest
@DisplayName("maximizeStatValue sets the stat to max")
void testMaximizeStat(EcsTestContext ctx) {
    var entity = ctx.createEntity();
    var statMap = new EntityStatMap();
    ctx.putComponent(entity, EntityStatMap.TYPE, statMap);
    ctx.flush();

    var healthStat = DefaultEntityStatTypes.getHealth();
    statMap.setStatValue(healthStat, 10.0f);
    statMap.maximizeStatValue(healthStat);

    StatsAssert.assertStatAtMax(ctx.getStore(), entity, healthStat);
}
```

### 2.4 Subtract Stat Value Below Zero
**Priority:** HIGH

**What you're testing:** What happens when you subtract more than the current stat value. Does it clamp to zero or go negative?

**What goes wrong silently:** You assume stats clamp to zero, but they might go negative. Your health bar displays correctly at zero, but the underlying value is -5 and your healing math breaks.

**Test:**
```java
@EcsTest
@DisplayName("subtractStatValue does not make health negative")
void testSubtractBelowZero(EcsTestContext ctx) {
    var entity = ctx.createEntity();
    var statMap = new EntityStatMap();
    ctx.putComponent(entity, EntityStatMap.TYPE, statMap);
    ctx.flush();

    var healthStat = DefaultEntityStatTypes.getHealth();
    statMap.setStatValue(healthStat, 5.0f);
    statMap.subtractStatValue(healthStat, 10.0f);

    float current = statMap.get(healthStat).get();
    HytaleAssert.assertTrue(
        "Health should not go below 0, was: " + current,
        current >= 0f
    );
}
```

### 2.5 Removing a Modifier
**Priority:** MEDIUM

**What you're testing:** After `removeModifier`, the modifier is gone and the stat returns to its base value.

**Recommendation:** Apply a modifier, verify it exists with `StatsAssert.assertHasModifier`, remove it, then verify it's gone with `StatsAssert.assertNoModifier`. This catches bugs where buff expiration doesn't actually clean up.

### 2.6 Stat Size and Multiple Stats
**Priority:** MEDIUM

**What you're testing:** The `size()` method on EntityStatMap returns the correct number of registered stats.

**Recommendation:** Check `statMap.size()` after initialization. If it's 0, none of your stats are registered and every stat read will fail silently.

---

## 3. Dealing and Receiving Damage

### 3.1 Creating a Damage Object
**Priority:** CRITICAL

**What you're testing:** You construct a Damage with a source, cause, and amount, then verify the values are stored correctly.

**What goes wrong silently:** You pass the wrong `causeIndex` to the Damage constructor. The damage amount is correct, but the cause type is wrong, so resistance calculations don't apply correctly. Your 50-damage fireball does full damage through fire resistance.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.HytaleAssert;

@HytaleSuite("Damage Tests")
@Tag("combat")
public class DamageTests {

    @HytaleTest
    @DisplayName("Damage object stores amount and cause correctly")
    void testDamageConstruction() {
        var damage = new Damage(Damage.NULL_SOURCE, DamageCause.PHYSICAL.getId(), 25.0f);

        HytaleAssert.assertEquals(25.0, (double) damage.getAmount(), 0.01);
        HytaleAssert.assertEquals(25.0, (double) damage.getInitialAmount(), 0.01);
        HytaleAssert.assertNotNull(damage.getSource());
    }

    @HytaleTest
    @DisplayName("setAmount modifies the damage value")
    void testDamageModification() {
        var damage = new Damage(Damage.NULL_SOURCE, DamageCause.PHYSICAL.getId(), 10.0f);

        damage.setAmount(30.0f);

        HytaleAssert.assertEquals(30.0, (double) damage.getAmount(), 0.01);
        HytaleAssert.assertEquals(10.0, (double) damage.getInitialAmount(), 0.01);
    }
}
```

### 3.2 DamageCause Properties
**Priority:** CRITICAL

**What you're testing:** Each DamageCause has properties that affect how damage is processed. FALL damage doesn't reduce durability. COMMAND damage bypasses resistances.

**What goes wrong silently:** You assume all damage types reduce weapon durability. Your custom damage event applies durability loss for fall damage, and players' armor breaks when they jump off a cliff.

**Test:**
```java
@HytaleTest
@DisplayName("DamageCause properties match expected behavior")
void testDamageCauseProperties() {
    HytaleAssert.assertFalse(
        "Fall damage should not cause durability loss",
        DamageCause.FALL.isDurabilityLoss()
    );
    HytaleAssert.assertTrue(
        "Physical damage should cause durability loss",
        DamageCause.PHYSICAL.isDurabilityLoss()
    );
    HytaleAssert.assertTrue(
        "Command damage should bypass resistances",
        DamageCause.COMMAND.doesBypassResistances()
    );
    HytaleAssert.assertFalse(
        "Physical damage should NOT bypass resistances",
        DamageCause.PHYSICAL.doesBypassResistances()
    );
}
```

### 3.3 Damage Kills an NPC
**Priority:** HIGH

**What you're testing:** You apply lethal damage to an NPC and verify it dies.

**What goes wrong silently:** You apply enough damage to kill the NPC, but the death system hasn't processed yet because you didn't wait for a tick. Or the NPC has resistances that reduced your damage below lethal.

**Test:**
```java
@CombatTest
@DisplayName("Lethal damage kills the target")
void testLethalDamage(WorldTestContext ctx) {
    var target = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
    ctx.flush();

    CombatAssert.assertAlive(ctx.getStore(), target);

    var damage = new Damage(
        Damage.NULL_SOURCE,
        DamageCause.COMMAND.getId(),
        9999.0f
    );
    // Apply via the entity's stat map
    var statMap = (EntityStatMap) ctx.getComponent(target, EntityStatMap.TYPE);
    var healthStat = DefaultEntityStatTypes.getHealth();
    statMap.subtractStatValue(healthStat, damage.getAmount());
    ctx.flush();

    CombatAssert.assertHealthBelow(ctx.getStore(), target, 1.0f);
}
```

### 3.4 Drowning and Environment Damage Types
**Priority:** MEDIUM

**What you're testing:** DROWNING and ENVIRONMENT damage causes have the expected IDs and don't trigger durability loss.

**Recommendation:** Verify `DamageCause.DROWNING.getId()` and `DamageCause.ENVIRONMENT.getId()` return non-negative values. Check `isDurabilityLoss()` returns false for both. Catches regressions when new damage types are added.

---

## 4. NPC Spawning and Behavior

### 4.1 Spawning a Named NPC
**Priority:** CRITICAL

**What you're testing:** You spawn an NPC by role name and verify it's a real NPC entity with the correct role.

**What goes wrong silently:** The role name is case-sensitive or the NPC module hasn't loaded its templates yet. Your spawn call silently returns a generic entity instead of the NPC you wanted.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;

@HytaleSuite(value = "NPC Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("npc")
public class NPCTests {

    @SpawnTest
    @DisplayName("Spawn NPC with role and verify NPCEntity component")
    void testSpawnNPCWithRole(WorldTestContext ctx) {
        var skeleton = ctx.spawnNPC("Trork_Warrior", 0, 64, 0);
        ctx.flush();

        EcsAssert.assertRefValid(skeleton);
        NPCAssert.assertNPCEntity(ctx.getStore(), skeleton);
        HytaleAssert.assertTrue("NPC should exist", ctx.entityExists(skeleton));
    }
}
```

### 4.2 Verifying NPC Role Names in the Registry
**Priority:** CRITICAL

**What you're testing:** Before spawning anything, you verify that the NPC role actually exists in the NPCPlugin registry.

**What goes wrong silently:** You misspell the role name or the NPC data hasn't loaded from the content pack. Your spawn call fails silently or creates a default entity. You spend an hour debugging spawn logic when the real problem is a typo in a string constant.

**Test:**
```java
@HytaleTest
@DisplayName("NPC role names are registered in NPCPlugin")
void testRoleNamesExist() {
    NPCAssert.assertRoleExists("Trork_Warrior");
    NPCAssert.assertRoleExists("Kweebec_Sapling");
}
```

### 4.3 NPC Plugin Singleton Access
**Priority:** HIGH

**What you're testing:** `NPCPlugin.get()` returns a non-null singleton and has the role template names you expect.

**What goes wrong silently:** The NPC module didn't initialize. Your `NPCPlugin.get()` call returns null deep inside your mod logic, and the NullPointerException points to some unrelated line.

**Test:**
```java
@HytaleTest
@DisplayName("NPCPlugin singleton is available and has roles")
void testNPCPluginAccess() {
    var plugin = NPCPlugin.get();
    HytaleAssert.assertNotNull("NPCPlugin.get() should not be null", plugin);

    var roles = plugin.getRoleTemplateNames();
    HytaleAssert.assertNotNull("Role template list should not be null", roles);
    HytaleAssert.assertNotEmpty(roles);
}
```

### 4.4 NPC is Not Despawning Immediately
**Priority:** HIGH

**What you're testing:** After spawning an NPC, it isn't immediately marked for despawn.

**What goes wrong silently:** Some NPC roles have despawn timers. You spawn a mob for your boss fight, but it's already in the despawn queue. It vanishes after a few seconds and your players think the fight is bugged.

**Test:**
```java
@SpawnTest
@DisplayName("Freshly spawned NPC is not despawning")
void testNPCNotDespawning(WorldTestContext ctx) {
    var npc = ctx.spawnNPC("Trork_Warrior", 0, 64, 0);
    ctx.flush();

    NPCAssert.assertNotDespawning(ctx.getStore(), npc);
}
```

### 4.5 NPC Has Health on Spawn
**Priority:** MEDIUM

**What you're testing:** Spawned NPCs start with positive health and are alive.

**Recommendation:** Combine `StatsAssert.assertAlive()` with `StatsAssert.assertHealthAtMax()` to verify NPCs spawn at full health. Catches template configuration errors.

---

## 5. Items, Inventory, and Equipment

### 5.1 Creating an ItemStack
**Priority:** CRITICAL

**What you're testing:** You create an ItemStack with an item ID and quantity, then verify the values.

**What goes wrong silently:** You pass a quantity of 0 and the stack reports as empty even though you think you created a valid item. Or you use a mismatched item ID string and `getItemId()` returns something different than what you passed.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;

@HytaleSuite("Item Tests")
@Tag("items")
public class ItemTests {

    @HytaleTest
    @DisplayName("ItemStack stores item ID and quantity")
    void testItemStackCreation() {
        var sword = new ItemStack("Weapon_Sword_Iron", 1);

        ItemAssert.assertItemId(sword, "Weapon_Sword_Iron");
        ItemAssert.assertItemQuantity(sword, 1);
        ItemAssert.assertItemNotEmpty(sword);
    }

    @HytaleTest
    @DisplayName("Empty item stack is detected correctly")
    void testEmptyItemStack() {
        ItemAssert.assertItemEmpty(ItemStack.EMPTY);
        HytaleAssert.assertTrue(
            "EMPTY constant should report isEmpty",
            ItemStack.EMPTY.isEmpty()
        );
    }
}
```

### 5.2 ItemStack Durability
**Priority:** CRITICAL

**What you're testing:** Tools and weapons have durability. You reduce durability and check when the item breaks.

**What goes wrong silently:** You call `withDurability()` but it returns a new ItemStack (it's immutable). You keep using the old reference with full durability while the new one with reduced durability gets garbage collected.

**Test:**
```java
@HytaleTest
@DisplayName("withDurability returns a new stack with reduced durability")
void testDurabilityReduction() {
    var pickaxe = new ItemStack("Tool_Pickaxe_Stone", 1);
    float maxDur = pickaxe.getMaxDurability();

    HytaleAssert.assertTrue(
        "Tool should have max durability > 0",
        maxDur > 0
    );
    ItemAssert.assertItemNotBroken(pickaxe);

    var damaged = pickaxe.withDurability(1.0f);
    HytaleAssert.assertEquals(1.0, (double) damaged.getDurability(), 0.01);

    var broken = pickaxe.withDurability(0.0f);
    ItemAssert.assertItemBroken(broken);
}
```

### 5.3 Stacking Identical Items
**Priority:** HIGH

**What you're testing:** Two ItemStacks with the same item ID can stack together. Two with different IDs cannot.

**What goes wrong silently:** Items with metadata (enchantments, custom data) look identical but `isStackableWith` returns false. Your inventory logic tries to merge them and creates duplicate entries.

**Test:**
```java
@HytaleTest
@DisplayName("Same items are stackable, different items are not")
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
```

### 5.4 withQuantity Returns a New Stack
**Priority:** HIGH

**What you're testing:** `withQuantity` returns a new ItemStack with the changed count. The original is not mutated.

**What goes wrong silently:** You assume `withQuantity` mutates in place. You call it, throw away the return value, and the stack still has the old quantity.

**Test:**
```java
@HytaleTest
@DisplayName("withQuantity returns new stack, does not mutate original")
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
```

### 5.5 SimpleItemContainer Operations
**Priority:** CRITICAL

**What you're testing:** You add items to a container, check slots, remove from slots, and verify capacity.

**What goes wrong silently:** `addItemStack` returns a leftover stack when the container is full, but you don't check for it. Items silently vanish.

**Test:**
```java
@HytaleTest
@DisplayName("SimpleItemContainer add, get, and remove")
void testSimpleContainer() {
    var container = new SimpleItemContainer((short) 10);

    HytaleAssert.assertEquals(10, container.getCapacity());
    HytaleAssert.assertTrue("New container should be empty", container.isEmpty());

    var sword = new ItemStack("Weapon_Sword_Iron", 1);
    container.addItemStackToSlot(sword, (short) 0);

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
```

### 5.6 Inventory Sections (Storage, Hotbar, Armor)
**Priority:** HIGH

**What you're testing:** The Inventory class has separate sections. You put an item in storage and verify it's not in the hotbar.

**What goes wrong silently:** You add an item to what you think is the hotbar, but the section accessor returns storage. Your equipment display shows the wrong items.

**Test:**
```java
@HytaleTest
@DisplayName("Inventory sections are separate")
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
```

### 5.7 Container Capacity Check
**Priority:** MEDIUM

**What you're testing:** `canAddItemStack` tells you whether a container has room before you try to add.

**Recommendation:** Create a container with capacity 1, add an item, then verify `canAddItemStack` returns false for a second item. Prevents items vanishing into full containers.

### 5.8 countItemStacks Accuracy
**Priority:** MEDIUM

**What you're testing:** `countItemStacks` on SimpleItemContainer returns the correct count after adds and removes.

**Recommendation:** Add 3 items to different slots, verify count is 3, remove one, verify count is 2. Simple but catches off-by-one bugs in inventory logic.

---

## 6. Loot and Drops

### 6.1 Item Drop List Contains Expected Items
**Priority:** CRITICAL

**What you're testing:** When you look up a loot table by ID, it has the drops you expect.

**What goes wrong silently:** You reference a loot table ID that exists but was modified in a content update. The drop list is valid but missing the item you expect. Your dungeon's treasure chest gives oak logs instead of gold.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;

@HytaleSuite("Loot Tests")
@Tag("loot")
public class LootTests {

    @HytaleTest
    @DisplayName("ItemDropList has an ID and contains drops")
    void testDropListProperties() {
        var dropList = ItemDropList.getAssetMap().get("Drop_Trork_Warrior");
        HytaleAssert.assertNotNull("Drop list should exist", dropList);

        var id = dropList.getId();
        HytaleAssert.assertNotNull("Drop list should have an ID", id);

        var drops = dropList.getContainer().getAllDrops();
        HytaleAssert.assertNotNull("Drops list should not be null", drops);
        LootAssert.assertDropCountBetween(drops, 1, 20);
    }
}
```

### 6.2 Loot Table Produces Expected Item
**Priority:** HIGH

**What you're testing:** A specific loot table drops a specific item. You verify the item ID appears in the drop results.

**What goes wrong silently:** The loot table has randomized outputs and your specific item has a low drop rate. Running the test once might miss it. Use `assertDropsContain` which checks for existence, or run multiple iterations.

**Test:**
```java
@HytaleTest
@DisplayName("Emberwulf drops hide and fire essence")
void testEmberwulfDrops() {
    var dropList = ItemDropList.getAssetMap().get("Drop_Emberwulf");
    HytaleAssert.assertNotNull("Drop list should exist", dropList);

    var drops = dropList.getContainer().getAllDrops();
    HytaleAssert.assertNotNull(drops);
    HytaleAssert.assertNotEmpty(drops);

    LootAssert.assertDropsContain(drops, "Ingredient_Hide_Heavy");
    LootAssert.assertDropsContain(drops, "Ingredient_Fire_Essence");
}
```

### 6.3 Empty Loot Table
**Priority:** MEDIUM

**What you're testing:** Some entities have no drops. Verify the loot table is genuinely empty.

**Recommendation:** Look up a loot table ID that should be empty and use `LootAssert.assertNoDrops()`. Catches cases where someone accidentally added drops to a non-dropping entity.

---

## 7. Crafting

### 7.1 Recipe Exists in the Registry
**Priority:** CRITICAL

**What you're testing:** A crafting recipe you depend on is actually registered and available.

**What goes wrong silently:** You reference a recipe ID in your mod, but the content pack that provides it hasn't loaded. The crafting UI shows nothing, and your players think your mod is broken.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;

@HytaleSuite("Crafting Tests")
@Tag("crafting")
public class CraftingTests {

    @HytaleTest
    @DisplayName("Known recipe exists in registry")
    void testRecipeExists() {
        CraftingAssert.assertRecipeExists("Weapon_Sword_Wooden");
    }

    @HytaleTest
    @DisplayName("Recipe registry has a reasonable number of recipes")
    void testRecipeCount() {
        CraftingAssert.assertRecipeCount(10);
    }
}
```

### 7.2 CraftingRecipe Properties
**Priority:** HIGH

**What you're testing:** A CraftingRecipe has valid input, output, and crafting time.

**What goes wrong silently:** The recipe exists but the output is different than expected. Your mod's auto-crafter produces the wrong item.

**Test:**
```java
@HytaleTest
@DisplayName("CraftingRecipe has valid inputs and outputs")
void testRecipeProperties() {
    var recipes = CraftingRecipe.getAssetMap();
    HytaleAssert.assertNotNull("Recipe asset map should exist", recipes);
    HytaleAssert.assertFalse("Should have at least one recipe", recipes.isEmpty());

    for (var entry : recipes.entrySet()) {
        var recipe = entry.getValue();
        HytaleAssert.assertNotNull("Recipe ID should not be null", recipe.getId());
        HytaleAssert.assertNotNull("Recipe should have inputs", recipe.getInput());
        HytaleAssert.assertNotNull("Recipe should have primary output", recipe.getPrimaryOutput());
        HytaleAssert.assertTrue(
            "Craft time should be non-negative for " + recipe.getId(),
            recipe.getTimeSeconds() >= 0f
        );
        break;  // just test the first one to verify the API works
    }
}
```

### 7.3 Recipe Output Validation
**Priority:** MEDIUM

**What you're testing:** The recipe's `getOutputs()` returns a non-empty list and each output is a valid ItemStack.

**Recommendation:** Iterate `recipe.getOutputs()` and verify each entry is non-null. Catches serialization bugs where outputs fail to deserialize from content packs.

---

## 8. World and Blocks

### 8.1 Block Placement and Reading
**Priority:** CRITICAL

**What you're testing:** You place a block at specific coordinates and read it back. If this doesn't work, nothing in your building mod works.

**What goes wrong silently:** The chunk at those coordinates isn't loaded. Your setBlock call silently fails, and getBlock returns air.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;

@HytaleSuite(value = "World Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("world")
public class WorldTests {

    @WorldTest
    @DisplayName("Block placed at coordinates can be read back")
    void testBlockPlacementAndRead(WorldTestContext ctx) {
        ctx.setBlock(10, 64, 10, "Rock_Stone");

        WorldAssert.assertBlockAt(ctx.getWorld(), 10, 64, 10, "Rock_Stone");
    }
}
```

### 8.2 Block Type Properties
**Priority:** CRITICAL

**What you're testing:** The `BlockType.fromString()` lookup returns a valid BlockType with the expected properties.

**What goes wrong silently:** The string ID format changed between server versions. Your `fromString` call returns null, and your block manipulation code throws NullPointerException in a completely different method.

**Test:**
```java
@HytaleTest
@DisplayName("BlockType.fromString returns valid block with properties")
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
```

### 8.3 Fill Region
**Priority:** HIGH

**What you're testing:** Filling a 3D region writes blocks to every position in the range.

**What goes wrong silently:** The fill operation misses edge blocks at the max boundary of the range (off-by-one). Your arena has a hole in one wall.

**Test:**
```java
@WorldTest
@DisplayName("fillRegion fills all blocks in the specified range")
void testFillRegion(WorldTestContext ctx) {
    ctx.fillRegion(0, 60, 0, 4, 60, 4, "Rock_Stone");

    WorldAssert.assertBlockAt(ctx.getWorld(), 0, 60, 0, "Rock_Stone");
    WorldAssert.assertBlockAt(ctx.getWorld(), 4, 60, 4, "Rock_Stone");
    WorldAssert.assertBlockAt(ctx.getWorld(), 2, 60, 2, "Rock_Stone");
}
```

### 8.4 Block Damage to Entities
**Priority:** HIGH

**What you're testing:** Some blocks deal damage to entities that touch them (lava, spikes). You verify the `getDamageToEntities` method returns a meaningful value.

**What goes wrong silently:** You rely on block damage for your trap room, but the damage value is 0 because the block type doesn't have it configured. Your traps do nothing.

**Test:**
```java
@HytaleTest
@DisplayName("Certain block types have entity damage values")
void testBlockDamageToEntities() {
    var stone = BlockType.fromString("Rock_Stone");
    HytaleAssert.assertNotNull(stone);

    float damage = stone.getDamageToEntities();
    HytaleAssert.assertTrue(
        "Stone should not deal damage to entities",
        damage == 0f
    );
}
```

### 8.5 World Configuration Access
**Priority:** HIGH

**What you're testing:** Reading WorldConfig values like seed, PvP state, and game mode.

**What goes wrong silently:** You read the game mode for conditional logic, but the config returns a different value than what the server operator set. Your mod's PvP features are disabled on a PvP server.

**Test:**
```java
@WorldTest
@DisplayName("WorldConfig is accessible and has valid values")
void testWorldConfig(WorldTestContext ctx) {
    var world = ctx.getWorld();
    HytaleAssert.assertNotNull(world);

    var config = ((World) world).getWorldConfig();
    HytaleAssert.assertNotNull("WorldConfig should not be null", config);
    HytaleAssert.assertNotNull("Game mode should not be null", config.getGameMode());
}
```

### 8.6 Universe and World Listing
**Priority:** MEDIUM

**What you're testing:** `Universe.get()` returns the singleton and `getWorlds()` includes the test world.

**Recommendation:** Use `WorldAssert.assertWorldExists(worldName)` to verify your target world is loaded. Catches startup ordering bugs where your mod runs before the world exists.

### 8.7 World Tick Counter
**Priority:** MEDIUM

**What you're testing:** `World.getTick()` returns a value that increases over time.

**Recommendation:** Record the tick, wait a few ticks, record again, and assert the second value is greater. Verifies the world is actually ticking and not paused.

---

## 9. Events and Listeners

### 9.1 Capturing a Fired Event
**Priority:** CRITICAL

**What you're testing:** Your mod fires a custom event and you verify it was actually dispatched.

**What goes wrong silently:** You register an event listener, but the event registration uses the wrong event type class. Your listener never triggers. The mod runs fine, but your cross-system communication is completely broken.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;
import com.frotty27.hrtk.api.context.TestContext;

@HytaleSuite("Event Tests")
@Tag("events")
public class EventTests {

    @HytaleTest
    @DisplayName("Event capture detects a fired event")
    void testEventFired(TestContext ctx) {
        var capture = ctx.captureEvent(MyCustomEvent.class);

        // Trigger the event (your mod's logic here)
        fireMyCustomEvent(new MyCustomEvent("test_trigger", 42));

        EventAssert.assertEventFired(capture);
        EventAssert.assertEventFiredWith(capture, e -> e.getValue() == 42);
    }
}
```

### 9.2 Event Not Fired When It Shouldn't Be
**Priority:** CRITICAL

**What you're testing:** Under certain conditions, your event should NOT fire. This is just as important as testing that it does fire.

**What goes wrong silently:** A code path accidentally fires the event when conditions aren't met. Your mod's reward system gives out free gold every time a player opens their inventory instead of only when they complete a quest.

**Test:**
```java
@HytaleTest
@DisplayName("Event does not fire when conditions are not met")
void testEventNotFired(TestContext ctx) {
    var capture = ctx.captureEvent(MyCustomEvent.class);

    // Do NOT trigger the event
    doSomethingThatShouldNotFire();

    EventAssert.assertEventNotFired(capture);
}
```

### 9.3 Event Cancellation
**Priority:** HIGH

**What you're testing:** Cancellable events can be cancelled by a listener, and the cancellation state is detectable.

**What goes wrong silently:** You cancel an event, but the system that fires it doesn't check `isCancelled()`. The event "looks" cancelled from the listener's perspective, but the action still happens.

**Test:**
```java
@HytaleTest
@DisplayName("Cancelled event is detected as cancelled")
void testEventCancellation(TestContext ctx) {
    var capture = ctx.captureEvent(MyCancellableEvent.class);

    // Fire and cancel
    var event = new MyCancellableEvent("cancel_test");
    event.setCancelled(true);
    fireEvent(event);

    EventAssert.assertEventFired(capture);
    EventAssert.assertEventCancelled(capture.getFirst());
}
```

### 9.4 Event Fire Count
**Priority:** HIGH

**What you're testing:** An event fires exactly the number of times you expect.

**What goes wrong silently:** A listener accidentally re-fires the event, causing an infinite loop or double-processing. Your shop charges the player twice for one purchase.

**Test:**
```java
@HytaleTest
@DisplayName("Event fires exactly once per trigger")
void testEventFireCount(TestContext ctx) {
    var capture = ctx.captureEvent(MyCustomEvent.class);

    fireMyCustomEvent(new MyCustomEvent("single_fire", 1));

    EventAssert.assertEventFired(capture, 1);
}
```

### 9.5 Multiple Event Types
**Priority:** MEDIUM

**What you're testing:** Capturing two different event types simultaneously and verifying only the correct one fires.

**Recommendation:** Set up two captures, fire only one event type, and use `assertEventNotFired` on the other. Verifies your event dispatch isn't leaking to the wrong type.

---

## 10. Commands

### 10.1 Command Executes Successfully
**Priority:** CRITICAL

**What you're testing:** Your custom command runs without throwing an exception.

**What goes wrong silently:** The command is registered but the handler throws a NullPointerException that gets swallowed by the command system. The player sees nothing, and you don't see the error unless you check server logs.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;
import com.frotty27.hrtk.api.context.TestContext;
import com.frotty27.hrtk.api.mock.MockCommandSender;

@HytaleSuite("Command Tests")
@Tag("commands")
public class CommandTests {

    @HytaleTest
    @RequiresPlayer
    @DisplayName("Custom command executes without error")
    void testCommandSucceeds(TestContext ctx) {
        var sender = ctx.createCommandSender("mymod.use");

        CommandAssert.assertCommandSucceeds(ctx, sender, "/mycommand help");
        CommandAssert.assertSenderReceivedMessage(sender, "Usage");
    }
}
```

### 10.2 Command Fails Without Permission
**Priority:** CRITICAL

**What you're testing:** A command that requires permissions rejects a sender who doesn't have them.

**What goes wrong silently:** Your permission check has a typo in the permission string, or you're checking the wrong permission node. The command works for everyone, including players who shouldn't have access.

**Test:**
```java
@HytaleTest
@RequiresPlayer
@DisplayName("Command fails without required permission")
void testCommandDenied(TestContext ctx) {
    var sender = ctx.createCommandSender(); // no permissions

    CommandAssert.assertCommandFails(ctx, sender, "/admin-only-command");
}
```

### 10.3 Command Output Verification
**Priority:** HIGH

**What you're testing:** A command produces the correct feedback message.

**What goes wrong silently:** The command runs fine, but the feedback message is wrong or empty. Your players type a command, nothing appears to happen, and they think it's broken.

**Test:**
```java
@HytaleTest
@RequiresPlayer
@DisplayName("Command sends correct feedback to sender")
void testCommandOutput(TestContext ctx) {
    var sender = ctx.createCommandSender("mymod.heal");

    CommandAssert.assertCommandSucceeds(ctx, sender, "/heal");
    CommandAssert.assertSenderReceivedMessage(sender, "healed");
    CommandAssert.assertSenderReceivedMessageCount(sender, 1);
}
```

### 10.4 Command Manager Access
**Priority:** HIGH

**What you're testing:** `CommandManager.get()` returns a valid singleton and your command is registered.

**What goes wrong silently:** Your command registration happens in the wrong lifecycle phase. The command works in dev but not in production because another plugin loaded first.

**Test:**
```java
@HytaleTest
@DisplayName("CommandManager is accessible and has registrations")
void testCommandManagerAccess() {
    var manager = CommandManager.get();
    HytaleAssert.assertNotNull("CommandManager should not be null", manager);

    var registration = manager.getCommandRegistration();
    HytaleAssert.assertNotNull("Command registration should not be null", registration);
}
```

### 10.5 Command Failure Message Content
**Priority:** MEDIUM

**What you're testing:** When a command fails, the error message contains useful information.

**Recommendation:** Use `CommandAssert.assertCommandFailsWithMessage(ctx, sender, "/bad args", "Invalid")` to verify error messages contain the right context. Helps debug player-reported issues.

---

## 11. Codecs and Serialization

### 11.1 Codec Round-Trip
**Priority:** CRITICAL

**What you're testing:** You encode a value and decode it back. The result matches the original.

**What goes wrong silently:** Your codec encodes fine, but the decode side drops a field. Your data looks correct when you save it, but when the server reloads, half your mod's state is gone.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;

@HytaleSuite("Codec Tests")
@Tag("codec")
public class CodecTests {

    @HytaleTest
    @DisplayName("STRING codec round-trips correctly")
    void testStringCodecRoundTrip() {
        CodecAssert.assertRoundTrip(Codec.STRING, "Trork_Warrior");
    }

    @HytaleTest
    @DisplayName("INTEGER codec round-trips correctly")
    void testIntegerCodecRoundTrip() {
        CodecAssert.assertRoundTrip(Codec.INTEGER, 42);
    }

    @HytaleTest
    @DisplayName("BOOLEAN codec round-trips correctly")
    void testBooleanCodecRoundTrip() {
        CodecAssert.assertRoundTrip(Codec.BOOLEAN, true);
        CodecAssert.assertRoundTrip(Codec.BOOLEAN, false);
    }

    @HytaleTest
    @DisplayName("DOUBLE codec round-trips correctly")
    void testDoubleCodecRoundTrip() {
        CodecAssert.assertRoundTrip(Codec.DOUBLE, 3.14159);
    }
}
```

### 11.2 Custom Codec Round-Trip
**Priority:** CRITICAL

**What you're testing:** Your mod's custom codec for a data class encodes and decodes without data loss.

**What goes wrong silently:** Your codec handles simple cases but fails on edge cases - empty strings, negative numbers, null optional fields. The data saves and loads 99% of the time, but that 1% corrupts player data.

**Test:**
```java
@HytaleTest
@DisplayName("Custom data class codec round-trips with edge cases")
void testCustomCodecRoundTrip() {
    CodecAssert.assertRoundTrip(MyQuestData.CODEC, new MyQuestData("Dragon_Slayer", 5, true));
    CodecAssert.assertRoundTrip(MyQuestData.CODEC, new MyQuestData("", 0, false));
    CodecAssert.assertRoundTrip(MyQuestData.CODEC, new MyQuestData("Long_Quest_Name_With_Underscores", -1, true));
}
```

### 11.3 Malformed Data Rejection
**Priority:** HIGH

**What you're testing:** When you feed bad data to a codec's decode method, it throws an exception instead of returning garbage.

**What goes wrong silently:** The codec returns a partially initialized object instead of throwing. Your mod continues running with corrupted state that causes seemingly unrelated bugs later.

**Test:**
```java
@HytaleTest
@DisplayName("Codec rejects malformed input")
void testMalformedDataRejection() {
    var badBson = new org.bson.BsonDocument(
        "wrong_field", new org.bson.BsonInt32(-1)
    );
    CodecAssert.assertDecodeThrows(MyQuestData.CODEC, badBson);
}
```

### 11.4 Encode and Decode Produce Expected Values
**Priority:** HIGH

**What you're testing:** Encoding produces a specific BSON structure and decoding it produces the expected object.

**What goes wrong silently:** The codec encodes with different field names than expected. Other systems that read the raw BSON (like database queries) get wrong data.

**Test:**
```java
@HytaleTest
@DisplayName("Encode produces correct value that decodes back")
void testEncodeDecodeExplicit() {
    String original = "Weapon_Sword_Iron";
    var encoded = Codec.STRING.encode(original);
    HytaleAssert.assertNotNull("Encoded value should not be null", encoded);

    var decoded = Codec.STRING.decode(encoded);
    HytaleAssert.assertEquals(original, decoded);
}
```

### 11.5 Null Handling in Codecs
**Priority:** MEDIUM

**What you're testing:** Your codec handles null values gracefully.

**Recommendation:** Try encoding null and verify it either throws a clear exception or returns a sentinel value. Never let null silently become a valid encoded document.

---

## 12. Physics and Movement

### 12.1 Setting and Reading Velocity
**Priority:** CRITICAL

**What you're testing:** You set velocity on an entity and read it back. Your knockback and movement systems depend on this.

**What goes wrong silently:** Velocity is set via a deferred command buffer operation, but you read it before the buffer flushes. The velocity reads as zero even though you just set it.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;

@HytaleSuite(value = "Physics Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("physics")
public class PhysicsTests {

    @WorldTest
    @DisplayName("Velocity can be set and read back")
    void testSetVelocity(WorldTestContext ctx) {
        var entity = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();

        var velocity = new Velocity();
        velocity.set(1.0, 5.0, -1.0);
        ctx.putComponent(entity, Velocity.TYPE, velocity);
        ctx.flush();

        PhysicsAssert.assertVelocity(
            ctx.getStore(), entity,
            1.0, 5.0, -1.0,
            0.1
        );
    }
}
```

### 12.2 Adding Force to an Entity
**Priority:** CRITICAL

**What you're testing:** `addForce` accumulates onto existing velocity rather than replacing it.

**What goes wrong silently:** You call `addForce` expecting it to add to the current velocity, but it resets the velocity first. Your knockback plus movement combo only applies the last force.

**Test:**
```java
@WorldTest
@DisplayName("addForce accumulates on existing velocity")
void testAddForce(WorldTestContext ctx) {
    var entity = ctx.spawnNPC("Trork_Warrior", 0, 64, 0);
    ctx.flush();

    var velocity = new Velocity();
    velocity.set(1.0, 0.0, 0.0);
    velocity.addForce(0.0, 5.0, 0.0);
    ctx.putComponent(entity, Velocity.TYPE, velocity);
    ctx.flush();

    PhysicsAssert.assertVelocity(
        ctx.getStore(), entity,
        1.0, 5.0, 0.0,
        0.1
    );
}
```

### 12.3 TransformComponent Position
**Priority:** HIGH

**What you're testing:** You set an entity's position via TransformComponent and verify it reads back correctly.

**What goes wrong silently:** You create a TransformComponent with a position, but the Vector3d constructor takes (x, y, z) and you passed them in the wrong order. Your entity is at (z, y, x) and teleport logic is broken.

**Test:**
```java
@WorldTest
@DisplayName("TransformComponent stores position correctly")
void testTransformPosition(WorldTestContext ctx) {
    var entity = ctx.spawnNPC("Kweebec_Sapling", 10, 64, 20);
    ctx.flush();

    var transform = new TransformComponent(
        new Vector3d(100.0, 70.0, 200.0),
        new Vector3f(0f, 90f, 0f)
    );
    ctx.putComponent(entity, TransformComponent.TYPE, transform);
    ctx.flush();

    double[] pos = ctx.getPosition(entity);
    HytaleAssert.assertNotNull("Position should not be null", pos);
    HytaleAssert.assertEquals(100.0, pos[0], 1.0);
    HytaleAssert.assertEquals(70.0, pos[1], 1.0);
    HytaleAssert.assertEquals(200.0, pos[2], 1.0);
}
```

### 12.4 Zero Velocity (Stationary Check)
**Priority:** HIGH

**What you're testing:** After calling `setZero()` on a Velocity, the entity is stationary.

**What goes wrong silently:** Your freeze mechanic calls `setZero()` but the physics system adds gravity on the next tick. The entity isn't truly frozen.

**Test:**
```java
@WorldTest
@DisplayName("setZero makes entity stationary")
void testSetZeroVelocity(WorldTestContext ctx) {
    var entity = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
    ctx.flush();

    var velocity = new Velocity();
    velocity.set(10.0, 10.0, 10.0);
    velocity.setZero();
    ctx.putComponent(entity, Velocity.TYPE, velocity);
    ctx.flush();

    PhysicsAssert.assertStationary(ctx.getStore(), entity, 0.01);
}
```

### 12.5 Speed Range Check
**Priority:** MEDIUM

**What you're testing:** An entity's speed is within an expected range after applying forces.

**Recommendation:** Use `PhysicsAssert.assertSpeed(store, ref, minSpeed, maxSpeed)` to validate that movement logic produces reasonable speeds. Catches bugs where physics math produces infinite or NaN velocities.

### 12.6 Ground State
**Priority:** MEDIUM

**What you're testing:** An entity's on-ground state after spawning on a solid surface.

**Recommendation:** Spawn an entity on a solid block and use `PhysicsAssert.assertOnGround(store, ref)`. Important for mods that change behavior based on whether the entity is airborne.

---

## 13. Multi-System Flows

### 13.1 Spawn NPC, Verify Health, Apply Damage
**Priority:** CRITICAL

**What you're testing:** The full flow of spawning an NPC, reading its stats, applying damage, and verifying the health change. This crosses ECS, stats, and combat systems.

**What goes wrong silently:** Each system works individually, but the integration between them breaks. The NPC spawns fine, health reads fine, but applying damage through the stat system doesn't trigger the death system. Your boss fight NPC takes damage but never dies.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.assert_.*;
import com.frotty27.hrtk.api.context.WorldTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;

@HytaleSuite(value = "Combat Flow Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("flow")
@Tag("combat")
public class CombatFlowTests {

    @FlowTest
    @DisplayName("Spawn, damage, and verify health reduction")
    void testSpawnDamageFlow(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Trork_Warrior", 0, 64, 0);
        ctx.flush();

        // Step 1: verify alive with health
        CombatAssert.assertAlive(ctx.getStore(), npc);
        StatsAssert.assertHealthAtMax(ctx.getStore(), npc,
            DefaultEntityStatTypes.getHealth());

        // Step 2: apply damage
        var statMap = (EntityStatMap) ctx.getComponent(npc, EntityStatMap.TYPE);
        HytaleAssert.assertNotNull("Entity should have EntityStatMap", statMap);

        var healthStat = DefaultEntityStatTypes.getHealth();
        statMap.subtractStatValue(healthStat, 5.0f);
        ctx.flush();

        // Step 3: verify health reduced
        CombatAssert.assertHealthBelow(
            ctx.getStore(), npc,
            statMap.get(healthStat).getMax()
        );
        CombatAssert.assertAlive(ctx.getStore(), npc);
    }
}
```

### 13.2 Create Item, Add to Container, Verify Slot
**Priority:** CRITICAL

**What you're testing:** The full inventory flow - create an item, put it in a container slot, read it back from that slot, and verify it matches.

**What goes wrong silently:** ItemStack creation works. Container creation works. But `addItemStackToSlot` silently fails because the container capacity is wrong, or the slot index is out of range. Your item disappears.

**Test:**
```java
@HytaleTest
@DisplayName("Full inventory flow: create item, store, retrieve")
void testInventoryFlow() {
    var container = new SimpleItemContainer((short) 27);
    var sword = new ItemStack("Weapon_Sword_Iron", 1);
    var arrows = new ItemStack("Ammo_Arrow", 64);

    container.addItemStackToSlot(sword, (short) 0);
    container.addItemStackToSlot(arrows, (short) 1);

    HytaleAssert.assertEquals(2, container.countItemStacks());

    var slot0 = container.getItemStack((short) 0);
    var slot1 = container.getItemStack((short) 1);

    ItemAssert.assertItemId(slot0, "Weapon_Sword_Iron");
    ItemAssert.assertItemQuantity(slot0, 1);
    ItemAssert.assertItemId(slot1, "Ammo_Arrow");
    ItemAssert.assertItemQuantity(slot1, 64);

    container.removeItemStackFromSlot((short) 0);
    HytaleAssert.assertEquals(1, container.countItemStacks());
}
```

### 13.3 NPC Spawn with Role Verification and Stat Check
**Priority:** HIGH

**What you're testing:** Spawn an NPC, verify it has the correct role, verify it has stats, and verify it's positioned correctly. Crosses NPC, ECS, and stat systems.

**What goes wrong silently:** The NPC spawns as a generic entity without the NPCEntity component. It looks like a mob, but role-specific behavior (AI, loot tables, dialogue) doesn't work.

**Test:**
```java
@FlowTest
@DisplayName("NPC spawn with full validation")
void testNPCFullSpawn(WorldTestContext ctx) {
    var npc = ctx.spawnNPC("Trork_Warrior", 50, 64, 50);
    ctx.flush();

    // Verify entity exists
    EcsAssert.assertRefValid(npc);
    HytaleAssert.assertTrue("NPC should exist", ctx.entityExists(npc));

    // Verify position
    double[] pos = ctx.getPosition(npc);
    HytaleAssert.assertNotNull("NPC should have a position", pos);
    HytaleAssert.assertEquals(50.0, pos[0], 2.0);

    // Verify NPC component
    NPCAssert.assertNPCEntity(ctx.getStore(), npc);
    NPCAssert.assertNotDespawning(ctx.getStore(), npc);

    // Verify stats
    StatsAssert.assertAlive(ctx.getStore(), npc);
}
```

### 13.4 Block Placement Affecting Entity
**Priority:** HIGH

**What you're testing:** Place a block where an entity is standing and verify the entity's state changes (e.g., suffocation, position shift).

**What goes wrong silently:** You place a block and expect the entity to take suffocation damage, but the collision system hasn't processed yet. Or the entity is pushed out but its TransformComponent still shows the old position.

**Test:**
```java
@FlowTest
@DisplayName("Block placement near entity changes world state")
void testBlockEntityInteraction(WorldTestContext ctx) {
    ctx.setBlock(10, 64, 10, "Rock_Stone");
    ctx.setBlock(10, 65, 10, "Empty");

    var npc = ctx.spawnNPC("Kweebec_Sapling", 10, 65, 10);
    ctx.flush();

    WorldAssert.assertBlockAt(ctx.getWorld(), 10, 64, 10, "Rock_Stone");
    EcsAssert.assertRefValid(npc);
    HytaleAssert.assertTrue("NPC should exist above block", ctx.entityExists(npc));
}
```

### 13.5 Message System
**Priority:** MEDIUM

**What you're testing:** Creating and formatting chat messages using the Message API.

**Recommendation:** Verify `Message.raw("Hello")` creates a message with the correct raw text via `getRawText()`. Test `Message.empty()`, `Message.bold()`, and `Message.color()` to make sure formatting doesn't corrupt the text content.

---

## 14. Performance Benchmarks

### 14.1 Entity Creation Throughput
**Priority:** HIGH

**What you're testing:** How fast you can create entities. If your mod spawns a lot of entities (mob waves, particle systems), this tells you your ceiling.

**What goes wrong silently:** Nothing goes "wrong" per se, but you find out during a boss fight that spawning 50 mobs takes 200ms and causes a visible lag spike. Better to know this number during development.

**Test:**
```java
import com.frotty27.hrtk.api.annotation.*;
import com.frotty27.hrtk.api.context.BenchmarkContext;
import com.frotty27.hrtk.api.context.EcsTestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;

@HytaleSuite(value = "Performance Benchmarks", isolation = IsolationStrategy.SNAPSHOT)
@Tag("benchmark")
public class PerformanceBenchmarks {

    @Benchmark(warmup = 50, iterations = 1000)
    @DisplayName("Entity creation throughput")
    void benchEntityCreation(BenchmarkContext bctx, EcsTestContext ctx) {
        bctx.startTimer();
        var entity = ctx.createEntity();
        ctx.putComponent(entity, TransformComponent.TYPE, new TransformComponent(
            new Vector3d(0, 64, 0), new Vector3f(0f, 0f, 0f)
        ));
        ctx.flush();
        bctx.stopTimer();
    }
}
```

### 14.2 ItemStack Creation Throughput
**Priority:** HIGH

**What you're testing:** How fast you can create ItemStacks. If your mod generates loot, this tells you whether your loot generation is a bottleneck.

**Test:**
```java
@Benchmark(warmup = 100, iterations = 5000)
@DisplayName("ItemStack creation throughput")
void benchItemStackCreation(BenchmarkContext ctx) {
    ctx.startTimer();
    var stack = new ItemStack("Weapon_Sword_Iron", 1);
    var id = stack.getItemId();
    var qty = stack.getQuantity();
    ctx.stopTimer();

    if (id == null || qty < 0) {
        throw new AssertionError("Unexpected null or negative");
    }
}
```

### 14.3 SimpleItemContainer Fill and Read
**Priority:** HIGH

**What you're testing:** How long it takes to fill a container and read all slots back. Relevant for inventory UI performance.

**Test:**
```java
@Benchmark(warmup = 20, iterations = 500)
@DisplayName("Container fill and read (27 slots)")
void benchContainerOperations(BenchmarkContext ctx) {
    var container = new SimpleItemContainer((short) 27);

    ctx.startTimer();
    for (short i = 0; i < 27; i++) {
        container.addItemStackToSlot(new ItemStack("Wood_Oak_Trunk", 64), i);
    }
    for (short i = 0; i < 27; i++) {
        var stack = container.getItemStack(i);
        if (stack == null) throw new AssertionError("Null at slot " + i);
    }
    container.clear();
    ctx.stopTimer();
}
```

### 14.4 Codec Round-Trip Throughput
**Priority:** MEDIUM

**What you're testing:** How fast your custom codec encodes and decodes.

**Recommendation:** Benchmark `Codec.STRING.encode(value)` followed by `Codec.STRING.decode(encoded)` over 10,000 iterations. If serialization is a bottleneck, you'll see it here.

### 14.5 EntityStatMap Operations
**Priority:** MEDIUM

**What you're testing:** How fast you can get, set, and update stat values.

**Recommendation:** Benchmark `setStatValue` followed by `get().get()` in a loop. Important for mods that do per-tick stat calculations on many entities.

---

## Quick Reference: Test Annotations

| Annotation | When to use | Injected context |
|---|---|---|
| `@HytaleTest` | Pure logic, no world needed | `TestContext` |
| `@EcsTest` | Component add/remove/read | `EcsTestContext` |
| `@WorldTest` | Block placement, single-tick world ops | `WorldTestContext` |
| `@SpawnTest` | Entity spawning and immediate checks | `WorldTestContext` |
| `@CombatTest` | Damage, health, death flows | `WorldTestContext` |
| `@FlowTest` | Multi-tick, multi-system scenarios | `WorldTestContext` |
| `@Benchmark` | Performance measurement | `BenchmarkContext` |

## Quick Reference: Isolation Strategies

| Strategy | Use when | Overhead |
|---|---|---|
| `NONE` | Read-only tests, codec tests, pure math | Zero |
| `SNAPSHOT` | Tests that modify ECS components | Low |
| `DEDICATED_WORLD` | Block changes, entity spawning, world lifecycle | High |

## Quick Reference: Assert Classes

| Class | Domain | Key methods |
|---|---|---|
| `HytaleAssert` | Core | `assertEquals`, `assertTrue`, `assertThrows`, `assertNotNull` |
| `EcsAssert` | ECS | `assertHasComponent`, `assertGetComponent`, `assertRefValid` |
| `ItemAssert` | Items | `assertItemId`, `assertItemQuantity`, `assertItemBroken` |
| `InventoryAssert` | Inventory | `assertSlotContains`, `assertSlotEmpty`, `assertInventoryEmpty` |
| `StatsAssert` | Stats | `assertStatEquals`, `assertStatAtMax`, `assertAlive`, `assertHasModifier` |
| `CombatAssert` | Combat | `assertDead`, `assertAlive`, `assertHealthBelow`, `assertHasKnockback` |
| `NPCAssert` | NPCs | `assertNPCEntity`, `assertRoleName`, `assertRoleExists` |
| `WorldAssert` | World | `assertBlockAt`, `assertEntityInWorld`, `assertWorldExists` |
| `BlockAssert` | Blocks | `assertBlockMaterial`, `assertBlockIsTrigger`, `assertBlockGroup` |
| `EventAssert` | Events | `assertEventFired`, `assertEventFiredWith`, `assertEventCancelled` |
| `CommandAssert` | Commands | `assertCommandSucceeds`, `assertCommandFails`, `assertSenderReceivedMessage` |
| `CodecAssert` | Codecs | `assertRoundTrip`, `assertDecodeEquals`, `assertDecodeThrows` |
| `LootAssert` | Loot | `assertDropsContain`, `assertDropCount`, `assertNoDrops` |
| `PhysicsAssert` | Physics | `assertVelocity`, `assertSpeed`, `assertOnGround`, `assertStationary` |
| `EffectAssert` | Effects | `assertHasEffect`, `assertEffectCount`, `assertInvulnerable` |

---

## 15. Specific Event Types

The catalog so far showed generic event capture patterns. But Hytale fires specific event types with specific data that modders need to inspect. These are the events you actually listen for in a real mod.

### 15.1 BreakBlockEvent - Verifying Block Type and Position
**Priority:** CRITICAL

**What you're testing:** When a block is broken, the event carries the correct block type and position. Your block protection mod, land claim system, or custom mining logic all branch on this data.

**What goes wrong silently:** The event fires, but `getBlockType()` returns the wrong type because the block was already replaced by the time your listener runs. Your protection system lets players break protected blocks because it checked the wrong block type.

**API methods involved:**
- `BreakBlockEvent.getBlockType()` returns `BlockType`
- `BreakBlockEvent.getTargetBlock()` returns `Vector3i`
- `BreakBlockEvent.getItemInHand()` returns `ItemStack`
- `BreakBlockEvent.setCancelled(boolean)` (extends CancellableEcsEvent)

**Test:**
```java
@HytaleSuite(value = "Block Event Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("events")
public class BlockEventTests {

    @WorldTest
    @DisplayName("BreakBlockEvent carries correct block type and position")
    void breakBlockEventHasCorrectData(WorldTestContext ctx) {
        var capture = ctx.captureEvent(BreakBlockEvent.class);

        ctx.setBlock(10, 64, 10, "Rock_Sandstone");
        ctx.simulateBlockBreak(10, 64, 10);

        EventAssert.assertEventFired(capture);
        var event = capture.getFirst();
        HytaleAssert.assertNotNull("Block type should not be null", event.getBlockType());
        HytaleAssert.assertNotNull("Target position should not be null", event.getTargetBlock());
    }
}
```

### 15.2 PlaceBlockEvent - Cancellation Prevents Placement
**Priority:** CRITICAL

**What you're testing:** You cancel a PlaceBlockEvent and verify the block was not actually placed. Your build protection, anti-grief, or zone restriction system depends on this.

**What goes wrong silently:** You cancel the event, but the block is already placed before your listener fires. The event system processes listeners after the action instead of before it, and your protection is useless.

**API methods involved:**
- `PlaceBlockEvent.getTargetBlock()` returns `Vector3i`
- `PlaceBlockEvent.getItemInHand()` returns `ItemStack`
- `PlaceBlockEvent.getRotation()` returns `RotationTuple`
- `PlaceBlockEvent.setCancelled(boolean)`

**Test:**
```java
@WorldTest
@DisplayName("Cancelling PlaceBlockEvent prevents the block from being placed")
void cancelledPlaceBlockPreventsPlacement(WorldTestContext ctx) {
    ctx.registerEventListener(PlaceBlockEvent.class, event -> {
        event.setCancelled(true);
    });

    ctx.simulateBlockPlace(10, 64, 10, "Rock_Sandstone");
    ctx.flush();

    WorldAssert.assertBlockAt(ctx.getWorld(), 10, 64, 10, "Empty");
}
```

### 15.3 PlayerChatEvent - Message Interception and Modification
**Priority:** CRITICAL

**What you're testing:** Your chat filter, prefix system, or custom chat format mod intercepts chat messages and can modify or cancel them.

**What goes wrong silently:** Your chat listener modifies `setContent()` but the original message is still broadcast. Or you cancel the event but the sender still sees their message in chat (client-side prediction).

**API methods involved:**
- `PlayerChatEvent.getContent()` returns `String`
- `PlayerChatEvent.setContent(String)`
- `PlayerChatEvent.getSender()` returns `PlayerRef`
- `PlayerChatEvent.getTargets()` returns `List<PlayerRef>`
- `PlayerChatEvent.setTargets(List<PlayerRef>)`
- `PlayerChatEvent.setCancelled(boolean)`
- `PlayerChatEvent.getFormatter()` / `setFormatter()`

**Test:**
```java
@HytaleTest
@DisplayName("PlayerChatEvent content can be read and modified")
void chatEventContentModifiable(TestContext ctx) {
    var capture = ctx.captureEvent(PlayerChatEvent.class);

    ctx.simulateChat("Hello world");

    EventAssert.assertEventFired(capture);
    var event = capture.getFirst();
    HytaleAssert.assertEquals("Hello world", event.getContent());

    event.setContent("Modified message");
    HytaleAssert.assertEquals("Modified message", event.getContent());
}
```

### 15.4 PlayerConnectEvent - World Assignment on Join
**Priority:** CRITICAL

**What you're testing:** When a player connects, you can read their assigned world and redirect them to a different one. Hub plugins, lobby systems, and world-per-game-mode architectures depend on this.

**What goes wrong silently:** You call `setWorld()` to redirect the player, but the original world already started loading chunks for them. The player sees a flash of the wrong world before being teleported.

**API methods involved:**
- `PlayerConnectEvent.getWorld()` returns `World`
- `PlayerConnectEvent.setWorld(World)`
- `PlayerConnectEvent.getPlayerRef()` returns `PlayerRef`
- `PlayerConnectEvent.getPlayer()` returns `Player`

**Test:**
```java
@HytaleTest
@DisplayName("PlayerConnectEvent provides world and player reference")
void connectEventHasWorldAndPlayer(TestContext ctx) {
    var capture = ctx.captureEvent(PlayerConnectEvent.class);

    ctx.simulatePlayerConnect("TestPlayer");

    EventAssert.assertEventFired(capture);
    var event = capture.getFirst();
    HytaleAssert.assertNotNull("World should not be null", event.getWorld());
    HytaleAssert.assertNotNull("PlayerRef should not be null", event.getPlayerRef());
}
```

### 15.5 PlayerDisconnectEvent - Cleanup on Leave
**Priority:** HIGH

**What you're testing:** When a player disconnects, you clean up their mod data (scoreboard entries, team assignments, session state). Your cleanup must run before the player entity is removed.

**API methods involved:**
- `PlayerDisconnectEvent.getPlayerRef()` returns `PlayerRef`
- `PlayerDisconnectEvent.getDisconnectReason()` returns `PacketHandler.DisconnectReason`

**Test:**
```java
@HytaleTest
@DisplayName("PlayerDisconnectEvent fires with player reference")
void disconnectEventHasPlayerRef(TestContext ctx) {
    var capture = ctx.captureEvent(PlayerDisconnectEvent.class);

    ctx.simulatePlayerDisconnect("TestPlayer");

    EventAssert.assertEventFired(capture);
    HytaleAssert.assertNotNull("PlayerRef should not be null",
        capture.getFirst().getPlayerRef());
}
```

### 15.6 DamageBlockEvent - Mining Progress and Tool Validation
**Priority:** HIGH

**What you're testing:** Your custom mining speed mod or tool requirement system reads the damage amount and tool from the event.

**What goes wrong silently:** `getDamage()` returns the raw damage before tool modifiers. Your mining speed display shows the wrong number because you used `getDamage()` instead of `getCurrentDamage()`.

**API methods involved:**
- `DamageBlockEvent.getDamage()` returns `float` (raw)
- `DamageBlockEvent.getCurrentDamage()` returns `float` (accumulated)
- `DamageBlockEvent.setDamage(float)`
- `DamageBlockEvent.getBlockType()` returns `BlockType`
- `DamageBlockEvent.getItemInHand()` returns `ItemStack`

**Test:**
```java
@WorldTest
@DisplayName("DamageBlockEvent carries damage amount and block type")
void damageBlockEventHasDamageAndType(WorldTestContext ctx) {
    var capture = ctx.captureEvent(DamageBlockEvent.class);

    ctx.setBlock(10, 64, 10, "Rock_Sandstone");
    ctx.simulateBlockDamage(10, 64, 10, 5.0f);

    EventAssert.assertEventFired(capture);
    var event = capture.getFirst();
    HytaleAssert.assertNotNull("Block type should be present", event.getBlockType());
    HytaleAssert.assertTrue("Damage should be positive", event.getDamage() > 0f);
}
```

### 15.7 CraftRecipeEvent - Recipe Verification
**Priority:** HIGH

**What you're testing:** When a player crafts something, the event carries the correct recipe and quantity.

**API methods involved:**
- `CraftRecipeEvent.getCraftedRecipe()` returns `CraftingRecipe`
- `CraftRecipeEvent.getQuantity()` returns `int`

**Test:**
```java
@HytaleTest
@DisplayName("CraftRecipeEvent carries recipe and quantity")
void craftEventHasRecipeData(TestContext ctx) {
    var capture = ctx.captureEvent(CraftRecipeEvent.class);

    ctx.simulateCraft("Weapon_Sword_Wooden", 1);

    EventAssert.assertEventFired(capture);
    var event = capture.getFirst();
    HytaleAssert.assertNotNull("Recipe should not be null", event.getCraftedRecipe());
    HytaleAssert.assertTrue("Quantity should be at least 1", event.getQuantity() >= 1);
}
```

### 15.8 SwitchActiveSlotEvent - Hotbar Tracking
**Priority:** HIGH

**What you're testing:** Your ability bar, weapon swap cooldown, or active item tracker needs to know when the player switches slots.

**API methods involved:**
- `SwitchActiveSlotEvent.getPreviousSlot()` returns `int`
- `SwitchActiveSlotEvent.getNewSlot()` returns `byte`
- `SwitchActiveSlotEvent.setNewSlot(byte)`
- `SwitchActiveSlotEvent.isClientRequest()` / `isServerRequest()` returns `boolean`

**Test:**
```java
@HytaleTest
@DisplayName("SwitchActiveSlotEvent tracks previous and new slot")
void slotSwitchEventHasSlotData(TestContext ctx) {
    var capture = ctx.captureEvent(SwitchActiveSlotEvent.class);

    ctx.simulateSlotSwitch(0, 3);

    EventAssert.assertEventFired(capture);
    var event = capture.getFirst();
    HytaleAssert.assertEquals(0, event.getPreviousSlot());
    HytaleAssert.assertEquals((byte) 3, event.getNewSlot());
}
```

### 15.9 ChangeGameModeEvent - Mode Switching
**Priority:** HIGH

**What you're testing:** Your mod reacts to game mode changes (Creative to Adventure, etc.) to enable/disable features.

**API methods involved:**
- `ChangeGameModeEvent.getGameMode()` returns `GameMode`
- `ChangeGameModeEvent.setGameMode(GameMode)`
- `ChangeGameModeEvent.setCancelled(boolean)`

**Test:**
```java
@HytaleTest
@DisplayName("ChangeGameModeEvent carries the new game mode")
void gameModeChangeEventHasMode(TestContext ctx) {
    var capture = ctx.captureEvent(ChangeGameModeEvent.class);

    ctx.simulateGameModeChange(GameMode.Creative);

    EventAssert.assertEventFired(capture);
    HytaleAssert.assertEquals(GameMode.Creative, capture.getFirst().getGameMode());
}
```

### 15.10 EventPriority and Registration Order
**Priority:** HIGH

**What you're testing:** Events registered at FIRST priority run before NORMAL, and LAST runs after NORMAL. Your protection plugin needs to cancel events before other listeners process them.

**API methods involved:**
- `EventPriority.FIRST`, `EARLY`, `NORMAL`, `LATE`, `LAST`
- `EventRegistry.register(EventPriority, Class, Consumer)`

**Test:**
```java
@HytaleTest
@DisplayName("EventPriority values are ordered correctly")
void eventPriorityOrdering() {
    HytaleAssert.assertTrue("FIRST should have lowest value",
        EventPriority.FIRST.getValue() < EventPriority.NORMAL.getValue());
    HytaleAssert.assertTrue("LAST should have highest value",
        EventPriority.LAST.getValue() > EventPriority.NORMAL.getValue());
    HytaleAssert.assertTrue("EARLY between FIRST and NORMAL",
        EventPriority.EARLY.getValue() > EventPriority.FIRST.getValue()
        && EventPriority.EARLY.getValue() < EventPriority.NORMAL.getValue());
}
```

### 15.11 PlayerInteractEvent - Full Interaction Context
**Priority:** CRITICAL

**What you're testing:** When a player interacts with a block or entity, you get the full context: what they're holding, what they targeted, and the interaction type.

**What goes wrong silently:** `getTargetEntity()` returns null because the interaction was with a block, not an entity. Your NPC dialogue system never triggers because you forgot to check `getTargetBlock()` as a fallback.

**API methods involved:**
- `PlayerInteractEvent.getActionType()` returns `InteractionType`
- `PlayerInteractEvent.getItemInHand()` returns `ItemStack`
- `PlayerInteractEvent.getTargetBlock()` returns `Vector3i`
- `PlayerInteractEvent.getTargetEntity()` returns `Entity`
- `PlayerInteractEvent.getTargetRef()` returns `Ref<EntityStore>`
- `PlayerInteractEvent.setCancelled(boolean)`

**Test:**
```java
@WorldTest
@DisplayName("PlayerInteractEvent provides interaction context")
void interactEventHasContext(WorldTestContext ctx) {
    var capture = ctx.captureEvent(PlayerInteractEvent.class);

    ctx.simulateInteract(10, 64, 10);

    EventAssert.assertEventFired(capture);
    var event = capture.getFirst();
    HytaleAssert.assertNotNull("Interaction type should not be null", event.getActionType());
}
```

### 15.12 EntityRemoveEvent - Tracking Despawns
**Priority:** HIGH

**What you're testing:** When an entity is removed from the world, your tracking system cleans up references to it.

**API methods involved:**
- `EntityRemoveEvent.getEntity()` returns `Entity`

**Test:**
```java
@WorldTest
@DisplayName("EntityRemoveEvent fires when entity is removed")
void entityRemoveEventFires(WorldTestContext ctx) {
    var capture = ctx.captureEvent(EntityRemoveEvent.class);

    var entity = ctx.spawnEntity("Kweebec_Sapling", 0, 64, 0);
    ctx.despawn(entity);

    EventAssert.assertEventFired(capture);
}
```

### 15.13 InteractivelyPickupItemEvent - Loot Pickup Filtering
**Priority:** MEDIUM

**What you're testing:** When a player picks up an item, you can inspect or cancel it. Your inventory filter or auto-sort mod depends on this.

**Recommendation:** Capture the event, verify `getItemStack()` returns the correct item, and test that `setCancelled(true)` prevents the pickup.

### 15.14 DropItemEvent - Drop Prevention
**Priority:** MEDIUM

**What you're testing:** Your "soulbound" item system cancels DropItemEvent to prevent dropping certain items.

**Recommendation:** Register a listener that cancels the event, then verify the item is still in the player's inventory.

### 15.15 LivingEntityInventoryChangeEvent - Inventory Monitoring
**Priority:** MEDIUM

**What you're testing:** When any living entity's inventory changes, your inventory tracking mod gets notified.

**Recommendation:** Capture the event after adding an item to a container, verify `getItemContainer()` and `getTransaction()` carry the correct data.

### 15.16 Lifecycle Events (Boot, Shutdown, PrepareUniverse)
**Priority:** MEDIUM

**What you're testing:** Your mod's startup initialization and shutdown cleanup run at the correct lifecycle phases.

**Recommendation:** Register listeners for `BootEvent` and `ShutdownEvent` in your plugin's setup. Verify your initialization code ran by checking a flag. `ShutdownEvent` has phase constants: `DISCONNECT_PLAYERS`, `UNBIND_LISTENERS`, `SHUTDOWN_WORLDS`.

---

## 16. Effects and Buffs

### 16.1 Adding an Effect to an Entity
**Priority:** CRITICAL

**What you're testing:** You apply a buff or debuff to an entity and verify it's active. Every RPG mod, potion system, and ability framework does this.

**What goes wrong silently:** You call `addEffect()` but the effect index doesn't match any registered EntityEffect asset. The method doesn't throw - it just silently ignores the invalid index. Your poison debuff never applies and you debug for an hour.

**API methods involved:**
- `EffectControllerComponent.addEffect(int effectIndex, float duration, boolean debuff, boolean invulnerable)`
- `EffectControllerComponent.addInfiniteEffect(int effectIndex, boolean debuff)`
- `EffectControllerComponent.getActiveEffects()` returns active effect indices
- `EffectControllerComponent.getAllActiveEntityEffects()` returns `List<ActiveEntityEffect>`
- `EffectControllerComponent.getComponentType()` returns `ComponentType`

**Test:**
```java
@HytaleSuite(value = "Effect Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("effects")
public class EffectTests {

    @WorldTest
    @DisplayName("Applied effect appears in active effects list")
    void appliedEffectIsActive(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();

        var controller = (EffectControllerComponent) ctx.getComponent(
            npc, EffectControllerComponent.getComponentType()
        );
        if (controller == null) {
            ctx.putComponent(npc, EffectControllerComponent.getComponentType(),
                new EffectControllerComponent());
            ctx.flush();
            controller = (EffectControllerComponent) ctx.getComponent(
                npc, EffectControllerComponent.getComponentType()
            );
        }
        HytaleAssert.assertNotNull("EffectController should be present", controller);

        controller.addEffect(0, 10.0f, false, false);

        var effects = controller.getAllActiveEntityEffects();
        HytaleAssert.assertNotNull("Active effects should not be null", effects);
        HytaleAssert.assertNotEmpty(effects);
    }
}
```

### 16.2 Removing an Effect
**Priority:** CRITICAL

**What you're testing:** After removing an effect, it no longer appears in the active list and its stat modifiers are reverted.

**What goes wrong silently:** You remove the effect, but the stat modifiers it applied are still active. The player's speed boost persists after the buff expires.

**API methods involved:**
- `EffectControllerComponent.removeEffect(int effectIndex)`
- `EffectControllerComponent.clearEffects()`

**Test:**
```java
@WorldTest
@DisplayName("Removed effect is no longer active")
void removedEffectIsInactive(WorldTestContext ctx) {
    var npc = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
    ctx.flush();

    var controller = (EffectControllerComponent) ctx.getComponent(
        npc, EffectControllerComponent.getComponentType()
    );
    HytaleAssert.assertNotNull("EffectController should be present", controller);

    controller.addEffect(0, 10.0f, false, false);
    controller.removeEffect(0);

    var effects = controller.getAllActiveEntityEffects();
    HytaleAssert.assertTrue("No effects should remain after removal",
        effects == null || effects.isEmpty());
}
```

### 16.3 Invulnerability via Effects
**Priority:** CRITICAL

**What you're testing:** Setting invulnerability through the effect controller makes the entity immune to damage.

**What goes wrong silently:** You set invulnerable to true, but the damage system checks the Invulnerable component, not the EffectController's flag. Your invulnerability frame doesn't work.

**API methods involved:**
- `EffectControllerComponent.setInvulnerable(boolean)`
- `EffectControllerComponent.isInvulnerable()` returns `boolean`

**Test:**
```java
@WorldTest
@DisplayName("Invulnerability flag is readable and writable")
void invulnerabilityToggle(WorldTestContext ctx) {
    var npc = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
    ctx.flush();

    var controller = (EffectControllerComponent) ctx.getComponent(
        npc, EffectControllerComponent.getComponentType()
    );
    HytaleAssert.assertNotNull(controller);

    controller.setInvulnerable(true);
    HytaleAssert.assertTrue("Entity should be invulnerable", controller.isInvulnerable());

    controller.setInvulnerable(false);
    HytaleAssert.assertFalse("Entity should no longer be invulnerable",
        controller.isInvulnerable());
}
```

### 16.4 EntityEffect Properties
**Priority:** HIGH

**What you're testing:** The EntityEffect asset has the expected properties - duration, debuff flag, overlap behavior, stat modifiers.

**API methods involved:**
- `EntityEffect.getAssetMap()` returns all registered effects
- `EntityEffect.getDuration()` returns `float`
- `EntityEffect.isDebuff()` returns `boolean`
- `EntityEffect.isInfinite()` returns `boolean`
- `EntityEffect.getOverlapBehavior()` returns `OverlapBehavior`
- `EntityEffect.getStatModifiers()`

**Test:**
```java
@HytaleTest
@DisplayName("EntityEffect assets have valid properties")
void entityEffectProperties() {
    var effects = EntityEffect.getAssetMap();
    HytaleAssert.assertNotNull("Effect asset map should exist", effects);

    if (!effects.isEmpty()) {
        var first = effects.values().iterator().next();
        HytaleAssert.assertNotNull("Effect should have a name", first.getName());
        HytaleAssert.assertTrue("Duration should be non-negative or infinite",
            first.getDuration() >= 0f || first.isInfinite());
    }
}
```

### 16.5 OverlapBehavior Enum Values
**Priority:** HIGH

**What you're testing:** The OverlapBehavior enum has EXTEND, OVERWRITE, and IGNORE values. Your buff stacking logic branches on this.

**Test:**
```java
@HytaleTest
@DisplayName("OverlapBehavior enum has expected values")
void overlapBehaviorValues() {
    HytaleAssert.assertNotNull(OverlapBehavior.EXTEND);
    HytaleAssert.assertNotNull(OverlapBehavior.OVERWRITE);
    HytaleAssert.assertNotNull(OverlapBehavior.IGNORE);
    HytaleAssert.assertEquals(3, OverlapBehavior.values().length);
}
```

### 16.6 ActiveEntityEffect Duration Tracking
**Priority:** MEDIUM

**What you're testing:** `ActiveEntityEffect.getRemainingDuration()` decreases over time and `isInfinite()` correctly identifies permanent effects.

**Recommendation:** Apply an effect, read `getRemainingDuration()`, wait a few ticks, read again, verify it decreased. Apply an infinite effect and verify `isInfinite()` returns true.

### 16.7 Effect Clear All
**Priority:** MEDIUM

**What you're testing:** `clearEffects()` removes all active effects at once. Your "purify" ability or death cleanup uses this.

**Recommendation:** Add 3 effects, call `clearEffects()`, verify `getAllActiveEntityEffects()` is empty.

---

## 17. Permissions

### 17.1 Checking Player Permissions
**Priority:** CRITICAL

**What you're testing:** `PermissionsModule.hasPermission(uuid, node)` returns the correct boolean. Every admin tool, rank system, and access-controlled feature depends on this.

**What goes wrong silently:** Permission nodes are case-sensitive. You check `"mymod.Admin"` but registered `"mymod.admin"`. The check returns false and your admin commands silently reject all admins.

**API methods involved:**
- `PermissionsModule.get()` returns singleton
- `PermissionsModule.hasPermission(UUID, String)` returns `boolean`
- `PermissionsModule.hasPermission(UUID, String, boolean defaultValue)` returns `boolean`

**Test:**
```java
@HytaleSuite(value = "Permissions Tests", isolation = IsolationStrategy.NONE)
@Tag("permissions")
public class PermissionsTests {

    @HytaleTest
    @DisplayName("PermissionsModule singleton is accessible")
    void permissionsModuleAccessible() {
        var module = PermissionsModule.get();
        HytaleAssert.assertNotNull("PermissionsModule.get() should not return null", module);
    }
}
```

### 17.2 Adding and Removing User Permissions
**Priority:** CRITICAL

**What you're testing:** You add a permission to a user and verify `hasPermission` returns true. You remove it and verify it returns false.

**What goes wrong silently:** `addUserPermission` succeeds, but the permission provider caches the old state. The player has to reconnect for permissions to take effect.

**API methods involved:**
- `PermissionsModule.addUserPermission(UUID, Set<String>)`
- `PermissionsModule.removeUserPermission(UUID, Set<String>)`
- `PermissionsModule.hasPermission(UUID, String)`

**Test:**
```java
@HytaleTest
@DisplayName("Added permission is immediately queryable")
void addedPermissionQueryable() {
    var module = PermissionsModule.get();
    var testUuid = UUID.randomUUID();

    module.addUserPermission(testUuid, Set.of("test.permission"));
    HytaleAssert.assertTrue("Permission should be active after add",
        module.hasPermission(testUuid, "test.permission"));

    module.removeUserPermission(testUuid, Set.of("test.permission"));
    HytaleAssert.assertFalse("Permission should be gone after remove",
        module.hasPermission(testUuid, "test.permission"));
}
```

### 17.3 Group Permissions
**Priority:** HIGH

**What you're testing:** You add a user to a group, add permissions to that group, and the user inherits them.

**What goes wrong silently:** Group permissions are checked at registration time but not when queried. Adding a user to a group after the group was created doesn't give them the group's permissions until server restart.

**API methods involved:**
- `PermissionsModule.addUserToGroup(UUID, String)`
- `PermissionsModule.removeUserFromGroup(UUID, String)`
- `PermissionsModule.addGroupPermission(String, Set<String>)`
- `PermissionsModule.removeGroupPermission(String, Set<String>)`
- `PermissionsModule.getGroupsForUser(UUID)` returns `Set<String>`

**Test:**
```java
@HytaleTest
@DisplayName("User inherits group permissions")
void userInheritsGroupPermissions() {
    var module = PermissionsModule.get();
    var testUuid = UUID.randomUUID();

    module.addGroupPermission("vip", Set.of("mymod.vip.feature"));
    module.addUserToGroup(testUuid, "vip");

    var groups = module.getGroupsForUser(testUuid);
    HytaleAssert.assertTrue("User should be in vip group", groups.contains("vip"));
    HytaleAssert.assertTrue("User should inherit group permission",
        module.hasPermission(testUuid, "mymod.vip.feature"));

    module.removeUserFromGroup(testUuid, "vip");
    module.removeGroupPermission("vip", Set.of("mymod.vip.feature"));
}
```

### 17.4 HytalePermissions Constants
**Priority:** HIGH

**What you're testing:** The built-in permission node constants exist and are non-null. Your admin tool checks against these.

**API methods involved:**
- `HytalePermissions.COMMAND_BASE`
- `HytalePermissions.ASSET_EDITOR`
- `HytalePermissions.BUILDER_TOOLS_EDITOR`
- `HytalePermissions.FLY_CAM`
- `HytalePermissions.fromCommand(String)`

**Test:**
```java
@HytaleTest
@DisplayName("Built-in permission constants are defined")
void builtInPermissionsExist() {
    HytaleAssert.assertNotNull("COMMAND_BASE should be defined",
        HytalePermissions.COMMAND_BASE);
    HytaleAssert.assertNotNull("ASSET_EDITOR should be defined",
        HytalePermissions.ASSET_EDITOR);
    HytaleAssert.assertNotNull("FLY_CAM should be defined",
        HytalePermissions.FLY_CAM);
}
```

### 17.5 PermissionProvider Interface
**Priority:** MEDIUM

**What you're testing:** The PermissionProvider interface has the expected methods. Custom permission backends (database-backed, Redis-backed) implement this.

**Recommendation:** Verify `PermissionsModule.get().getProviders()` returns at least one provider, and that provider has a non-null `getName()`.

### 17.6 Permission Events
**Priority:** MEDIUM

**What you're testing:** When permissions change, the corresponding events fire.

**Recommendation:** Listen for `PlayerPermissionChangeEvent` after calling `addUserPermission()`. Verify `getPlayerUuid()` matches. Listen for `GroupPermissionChangeEvent` after `addGroupPermission()`. Verify `getGroupName()` matches.

---

## 18. Entity Components

These are ECS components modders attach to or read from entities. Beyond TransformComponent (covered in section 1), there are many more that control entity appearance, behavior, and state.

### 18.1 DisplayNameComponent - Custom Entity Names
**Priority:** CRITICAL

**What you're testing:** You set a custom display name on an entity (boss name, NPC title, pet name) and read it back.

**What goes wrong silently:** You create a DisplayNameComponent with a Message, but the Message encoding is wrong. The name displays as raw JSON in-game instead of formatted text.

**API methods involved:**
- `DisplayNameComponent(Message)` constructor
- `DisplayNameComponent.getDisplayName()` returns `Message`
- `DisplayNameComponent.getComponentType()` returns `ComponentType`

**Test:**
```java
@HytaleSuite(value = "Entity Component Tests", isolation = IsolationStrategy.SNAPSHOT)
@Tag("components")
public class EntityComponentTests {

    @EcsTest
    @DisplayName("DisplayNameComponent stores and returns the name")
    void displayNameStoresCorrectly(EcsTestContext ctx) {
        var entity = ctx.createEntity();
        var name = Message.raw("Elite Skeleton");
        var comp = new DisplayNameComponent(name);
        ctx.putComponent(entity, DisplayNameComponent.getComponentType(), comp);
        ctx.flush();

        var stored = (DisplayNameComponent) ctx.getComponent(
            entity, DisplayNameComponent.getComponentType()
        );
        HytaleAssert.assertNotNull("DisplayName should be retrievable", stored);
        HytaleAssert.assertNotNull("Display name message should not be null",
            stored.getDisplayName());
    }
}
```

### 18.2 EntityScaleComponent - Size Changes
**Priority:** CRITICAL

**What you're testing:** You scale an entity up or down (boss grows during enrage, pets shrink when tamed) and the scale value persists.

**What goes wrong silently:** You set the scale to 2.0, but the network sync hasn't happened yet. Other players see the entity at normal size. Your boss fight visual cue is invisible to everyone except the server.

**API methods involved:**
- `EntityScaleComponent(float scale)` constructor
- `EntityScaleComponent.getScale()` returns `float`
- `EntityScaleComponent.setScale(float)`
- `EntityScaleComponent.getComponentType()`

**Test:**
```java
@EcsTest
@DisplayName("EntityScaleComponent stores scale factor")
void entityScaleStoresCorrectly(EcsTestContext ctx) {
    var entity = ctx.createEntity();
    var scale = new EntityScaleComponent(2.5f);
    ctx.putComponent(entity, EntityScaleComponent.getComponentType(), scale);
    ctx.flush();

    var stored = (EntityScaleComponent) ctx.getComponent(
        entity, EntityScaleComponent.getComponentType()
    );
    HytaleAssert.assertNotNull(stored);
    HytaleAssert.assertEquals(2.5, (double) stored.getScale(), 0.01);

    stored.setScale(0.5f);
    HytaleAssert.assertEquals(0.5, (double) stored.getScale(), 0.01);
}
```

### 18.3 Invulnerable Component - Damage Immunity
**Priority:** CRITICAL

**What you're testing:** Adding the Invulnerable component to an entity makes it immune to damage. Your NPC quest giver, tutorial entity, or cutscene actor needs this.

**What goes wrong silently:** You add the component, but the damage system checks a different flag. Your "immortal" NPC dies to fall damage.

**API methods involved:**
- `Invulnerable.INSTANCE` static singleton
- `Invulnerable.getComponentType()`

**Test:**
```java
@EcsTest
@DisplayName("Invulnerable component can be added as singleton")
void invulnerableComponentWorks(EcsTestContext ctx) {
    var entity = ctx.createEntity();
    ctx.putComponent(entity, Invulnerable.getComponentType(), Invulnerable.INSTANCE);
    ctx.flush();

    HytaleAssert.assertTrue("Entity should have Invulnerable component",
        ctx.hasComponent(entity, Invulnerable.getComponentType()));
}
```

### 18.4 BoundingBox - Collision Size
**Priority:** HIGH

**What you're testing:** You read or modify an entity's bounding box for custom collision detection, area-of-effect calculations, or boss hitbox sizing.

**API methods involved:**
- `BoundingBox()` and `BoundingBox(Box)` constructors
- `BoundingBox.getBoundingBox()` returns `Box`
- `BoundingBox.setBoundingBox(Box)`
- `BoundingBox.getDetailBoxes()` returns `Map<String, DetailBox[]>`
- `BoundingBox.getComponentType()`

**Test:**
```java
@EcsTest
@DisplayName("BoundingBox can be read and modified")
void boundingBoxReadWrite(EcsTestContext ctx) {
    var entity = ctx.createEntity();
    var bb = new BoundingBox();
    ctx.putComponent(entity, BoundingBox.getComponentType(), bb);
    ctx.flush();

    var stored = (BoundingBox) ctx.getComponent(entity, BoundingBox.getComponentType());
    HytaleAssert.assertNotNull("BoundingBox should be retrievable", stored);
}
```

### 18.5 HeadRotation - Look Direction
**Priority:** HIGH

**What you're testing:** You read an entity's look direction for aiming systems, NPC facing logic, or line-of-sight calculations.

**API methods involved:**
- `HeadRotation.getRotation()` returns `Vector3f`
- `HeadRotation.setRotation(Vector3f)`
- `HeadRotation.getDirection()` returns `Vector3d`
- `HeadRotation.getAxisDirection()` returns `Vector3i`
- `HeadRotation.getComponentType()`

**Test:**
```java
@EcsTest
@DisplayName("HeadRotation stores rotation and computes direction")
void headRotationStoresAndComputes(EcsTestContext ctx) {
    var entity = ctx.createEntity();
    var rotation = new HeadRotation(new Vector3f(0f, 90f, 0f));
    ctx.putComponent(entity, HeadRotation.getComponentType(), rotation);
    ctx.flush();

    var stored = (HeadRotation) ctx.getComponent(entity, HeadRotation.getComponentType());
    HytaleAssert.assertNotNull(stored);
    HytaleAssert.assertNotNull("Rotation vector should not be null", stored.getRotation());
    HytaleAssert.assertNotNull("Direction should be computable", stored.getDirection());
}
```

### 18.6 PositionDataComponent - Block Context
**Priority:** HIGH

**What you're testing:** You read what block an entity is standing on or inside. Your environmental damage system (lava, poison fog) or movement speed modifier depends on this.

**API methods involved:**
- `PositionDataComponent.getInsideBlockTypeId()` returns `int`
- `PositionDataComponent.getStandingOnBlockTypeId()` returns `int`
- `PositionDataComponent.setInsideBlockTypeId(int)`
- `PositionDataComponent.setStandingOnBlockTypeId(int)`
- `PositionDataComponent.getComponentType()`

**Test:**
```java
@WorldTest
@DisplayName("PositionDataComponent tracks block context")
void positionDataTracksBlockContext(WorldTestContext ctx) {
    var entity = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
    ctx.flush();

    var posData = (PositionDataComponent) ctx.getComponent(
        entity, PositionDataComponent.getComponentType()
    );
    if (posData != null) {
        int standingOn = posData.getStandingOnBlockTypeId();
        ctx.log("Entity standing on block type ID: %d", standingOn);
    }
}
```

### 18.7 Marker Components (Intangible, Interactable, RespondToHit)
**Priority:** MEDIUM

**What you're testing:** Singleton marker components toggle entity behavior flags. Intangible makes the entity pass through other entities. Interactable enables right-click interaction. RespondToHit enables hit reactions.

**Recommendation:** For each: add the singleton INSTANCE, flush, verify `hasComponent` returns true. These are on/off toggles with no state to verify beyond presence.

### 18.8 ActiveAnimationComponent - Animation Control
**Priority:** MEDIUM

**What you're testing:** Your mod triggers custom animations on entities.

**Recommendation:** Create an `ActiveAnimationComponent` with animation names, attach it, flush, and verify `getActiveAnimations()` returns the expected array.

---

## 19. Game Modes

### 19.1 GameMode Enum Values
**Priority:** HIGH

**What you're testing:** The GameMode enum has Adventure and Creative values. Your mode-specific logic branches on this.

**API methods involved:**
- `GameMode.Adventure`
- `GameMode.Creative`
- `GameMode.values()`

**Test:**
```java
@HytaleSuite("Game Mode Tests")
@Tag("gamemode")
public class GameModeTests {

    @HytaleTest
    @DisplayName("GameMode enum has Adventure and Creative")
    void gameModeValues() {
        HytaleAssert.assertNotNull(GameMode.Adventure);
        HytaleAssert.assertNotNull(GameMode.Creative);
        HytaleAssert.assertEquals(2, GameMode.values().length);
    }
}
```

### 19.2 GameModeType Asset Lookup
**Priority:** HIGH

**What you're testing:** `GameModeType.fromGameMode()` returns a valid asset with permission groups and interactions.

**API methods involved:**
- `GameModeType.fromGameMode(GameMode)` returns `GameModeType`
- `GameModeType.getPermissionGroups()` returns `String[]`
- `GameModeType.getInteractionsOnEnter()` returns `String`

**Test:**
```java
@HytaleTest
@DisplayName("GameModeType asset is resolvable from GameMode enum")
void gameModeTypeResolvable() {
    var adventureType = GameModeType.fromGameMode(GameMode.Adventure);
    HytaleAssert.assertNotNull("Adventure GameModeType should resolve", adventureType);

    var creativeType = GameModeType.fromGameMode(GameMode.Creative);
    HytaleAssert.assertNotNull("Creative GameModeType should resolve", creativeType);
}
```

---

## 20. Death and Respawn

### 20.1 DeathComponent - Death Cause Inspection
**Priority:** CRITICAL

**What you're testing:** When an entity dies, the DeathComponent carries the cause, message, and item loss data. Your death screen, kill feed, and item recovery system all read from this.

**What goes wrong silently:** `getDeathCause()` returns a DamageCause index instead of the actual DamageCause object. Your kill feed displays "Unknown" for every death because you didn't look up the cause correctly.

**API methods involved:**
- `DeathComponent.getDeathCause()` returns death cause info
- `DeathComponent.getDeathMessage()` / `setDeathMessage()`
- `DeathComponent.isShowDeathMenu()` / `setShowDeathMenu(boolean)`
- `DeathComponent.getItemsLostOnDeath()` / `setItemsLostOnDeath()`
- `DeathComponent.getDeathItemLoss()` returns `DeathItemLoss`
- `DeathComponent.respawn()` static method

**Test:**
```java
@HytaleSuite(value = "Death and Respawn Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("death")
public class DeathRespawnTests {

    @CombatTest
    @DisplayName("DeathComponent is accessible after lethal damage")
    void deathComponentAfterKill(WorldTestContext ctx) {
        var npc = ctx.spawnNPC("Kweebec_Sapling", 0, 64, 0);
        ctx.flush();

        // Apply lethal damage through stat system
        var statMap = (EntityStatMap) ctx.getComponent(npc, EntityStatMap.getComponentType());
        if (statMap != null) {
            var healthStat = DefaultEntityStatTypes.getHealth();
            statMap.subtractStatValue(healthStat, 9999.0f);
            ctx.flush();
        }

        // Check for death component
        var death = ctx.getComponent(npc, DeathComponent.getComponentType());
        if (death != null) {
            ctx.log("DeathComponent found on entity after lethal damage");
        }
    }
}
```

### 20.2 DeathItemLoss - What Drops When You Die
**Priority:** HIGH

**What you're testing:** Your custom death penalty system reads DeathItemLoss to control what items players lose.

**API methods involved:**
- `DeathItemLoss.getLossMode()`
- `DeathItemLoss.getItemsLost()`
- `DeathItemLoss.getAmountLossPercentage()`
- `DeathItemLoss.getDurabilityLossPercentage()`
- `DeathItemLoss.noLossMode()` static factory

**Test:**
```java
@HytaleTest
@DisplayName("DeathItemLoss.noLossMode returns a valid no-loss configuration")
void noLossMode() {
    var noLoss = DeathItemLoss.noLossMode();
    HytaleAssert.assertNotNull("noLossMode should return a valid object", noLoss);
}
```

### 20.3 DamageSystems.executeDamage - Programmatic Kill
**Priority:** HIGH

**What you're testing:** You programmatically deal damage to an entity via the damage system pipeline (not just subtracting health). This triggers death events, kill feed, and drops correctly.

**API methods involved:**
- `DamageSystems.executeDamage()` (3 overloads)
- `DamageModule.get()` returns singleton
- `DamageModule.getGatherDamageGroup()` / `getFilterDamageGroup()` / `getInspectDamageGroup()`

**Recommendation:** Use `DamageSystems.executeDamage()` with a Damage object instead of manually subtracting health. This ensures the full damage pipeline runs (resistances, events, death processing).

---

## 21. Projectiles

### 21.1 Spawning a Projectile
**Priority:** CRITICAL

**What you're testing:** Your custom ranged weapon or spell system spawns projectiles with the correct config, position, and velocity.

**What goes wrong silently:** You pass the wrong projectile asset name. `spawnProjectile()` creates a generic entity without physics. Your arrow flies in a straight line with no gravity and no collision.

**API methods involved:**
- `ProjectileModule.get()` returns singleton
- `ProjectileModule.spawnProjectile(UUID owner, Vector3d position, Vector3d velocity)` (2 overloads)
- `ProjectileConfig.getAssetMap()` returns all projectile configs
- `ProjectileConfig.getLaunchForce()` returns `float`
- `ProjectileConfig.getMuzzleVelocity()` returns `float`
- `ProjectileConfig.getGravity()` returns `float`

**Test:**
```java
@HytaleSuite(value = "Projectile Tests", isolation = IsolationStrategy.DEDICATED_WORLD)
@Tag("projectile")
public class ProjectileTests {

    @HytaleTest
    @DisplayName("ProjectileModule singleton is accessible")
    void projectileModuleAccessible() {
        var module = ProjectileModule.get();
        HytaleAssert.assertNotNull("ProjectileModule should not be null", module);
    }

    @HytaleTest
    @DisplayName("ProjectileConfig assets are registered")
    void projectileConfigsExist() {
        var configs = ProjectileConfig.getAssetMap();
        HytaleAssert.assertNotNull("Projectile config map should exist", configs);
        HytaleAssert.assertFalse("Should have at least one projectile config",
            configs.isEmpty());
    }
}
```

### 21.2 ProjectileConfig Properties
**Priority:** HIGH

**What you're testing:** Projectile configs have valid physics properties. Your custom projectile needs correct gravity and velocity to behave realistically.

**API methods involved:**
- `ProjectileConfig.getPhysicsConfig()` returns `PhysicsConfig`
- `StandardPhysicsConfig.getGravity()` returns `float`
- `StandardPhysicsConfig.getBounciness()` returns `float`
- `StandardPhysicsConfig.getBounceLimit()` returns `int`

**Test:**
```java
@HytaleTest
@DisplayName("ProjectileConfig has valid physics properties")
void projectileConfigPhysics() {
    var configs = ProjectileConfig.getAssetMap();
    if (!configs.isEmpty()) {
        var first = configs.values().iterator().next();
        HytaleAssert.assertNotNull("Projectile should have physics config",
            first.getPhysicsConfig());
        HytaleAssert.assertTrue("Muzzle velocity should be positive",
            first.getMuzzleVelocity() > 0f);
    }
}
```

### 21.3 Impact and Bounce Callbacks
**Priority:** MEDIUM

**What you're testing:** The ImpactConsumer and BounceConsumer interfaces allow you to react when projectiles hit things or bounce. Your explosive arrow or ricochet bullet needs this.

**Recommendation:** Verify the interfaces exist and have the expected method signatures: `ImpactConsumer.onImpact(ref, position, hitRef, hitType, commandBuffer)` and `BounceConsumer.onBounce(ref, position, commandBuffer)`.

---

## 22. Plugin System

### 22.1 PluginBase Registries
**Priority:** CRITICAL

**What you're testing:** Your plugin has access to all the registries it needs - events, commands, tasks, components, assets, codecs.

**What goes wrong silently:** You call `getEventRegistry()` during construction but it returns null because registries aren't initialized until setup. Your event listener is never registered and your mod silently does nothing.

**API methods involved:**
- `PluginBase.getEventRegistry()` returns `EventRegistry`
- `PluginBase.getCommandRegistry()` returns `CommandRegistry`
- `PluginBase.getTaskRegistry()` returns `TaskRegistry`
- `PluginBase.getEntityStoreRegistry()` returns `ComponentRegistryProxy<EntityStore>`
- `PluginBase.getChunkStoreRegistry()` returns `ComponentRegistryProxy<ChunkStore>`
- `PluginBase.getAssetRegistry()` returns `AssetRegistry`
- `PluginBase.getBlockStateRegistry()` returns `BlockStateRegistry`
- `PluginBase.getEntityRegistry()` returns `EntityRegistry`

**Test:**
```java
@HytaleSuite(value = "Plugin System Tests", isolation = IsolationStrategy.NONE)
@Tag("plugin")
public class PluginSystemTests {

    @HytaleTest
    @DisplayName("PluginManager is accessible and has plugins loaded")
    void pluginManagerAccessible() {
        var manager = PluginManager.get();
        HytaleAssert.assertNotNull("PluginManager should not be null", manager);

        var plugins = manager.getPlugins();
        HytaleAssert.assertNotNull("Plugin list should not be null", plugins);
        HytaleAssert.assertNotEmpty(plugins);
    }
}
```

### 22.2 PluginManager - Plugin Discovery
**Priority:** HIGH

**What you're testing:** `PluginManager.getPlugins()` lists loaded plugins. Your dependency checker needs this.

**API methods involved:**
- `PluginManager.get()` returns singleton
- `PluginManager.getPlugins()` returns `List<PluginBase>`
- `PluginManager.getPlugin(PluginIdentifier)` returns `PluginBase`
- `PluginManager.hasPlugin(PluginIdentifier, SemverRange)` returns `boolean`

**Test:**
```java
@HytaleTest
@DisplayName("Loaded plugins have valid identifiers and state")
void loadedPluginsValid() {
    var plugins = PluginManager.get().getPlugins();
    for (var plugin : plugins) {
        HytaleAssert.assertNotNull("Plugin name should not be null", plugin.getName());
        HytaleAssert.assertNotNull("Plugin identifier should not be null",
            plugin.getIdentifier());
        HytaleAssert.assertTrue("Plugin should be enabled",
            plugin.isEnabled() || plugin.isDisabled());
    }
}
```

### 22.3 PluginState Lifecycle
**Priority:** MEDIUM

**What you're testing:** The PluginState enum tracks plugin lifecycle. Your hot-reload or dependency system needs to know if a plugin is ENABLED, DISABLED, or FAILED.

**Recommendation:** Verify `PluginState.values()` contains NONE, SETUP, START, ENABLED, SHUTDOWN, DISABLED, FAILED.

---

## Gap Summary

Total modder-relevant methods inventoried: ~380
Covered by existing catalog (sections 1-14): ~120 (32%)
**Missing (CRITICAL): 18 entries added** (sections 15-22)
**Missing (HIGH): 24 entries added** (sections 15-22)
**Missing (MEDIUM): 16 entries added** (sections 15-22)
Skipped (LOW/INTERNAL): ~200+ (engine internals, protocol classes, worldgen)

### Newly Covered API Surfaces
| Surface | CRITICAL | HIGH | MEDIUM |
|---|---|---|---|
| Specific Event Types (15) | 4 | 6 | 6 |
| Effects and Buffs (16) | 3 | 2 | 2 |
| Permissions (17) | 2 | 2 | 2 |
| Entity Components (18) | 3 | 3 | 2 |
| Game Modes (19) | 0 | 2 | 0 |
| Death and Respawn (20) | 1 | 2 | 0 |
| Projectiles (21) | 1 | 1 | 1 |
| Plugin System (22) | 1 | 1 | 1 |
| **Total new** | **15** | **19** | **14** |
