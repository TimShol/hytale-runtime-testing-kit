package com.frotty27.hrtk.server.surface;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Method;

public final class InventoryTestAdapter {

    private InventoryTestAdapter() {}

    public static Inventory getInventory(Store<EntityStore> store, Ref<EntityStore> ref) {
        String[] candidateClasses = {
            "com.hypixel.hytale.server.core.entity.entities.Player",
            "com.hypixel.hytale.server.core.entity.LivingEntity"
        };

        for (String className : candidateClasses) {
            try {
                Class<?> componentClass = Class.forName(className);
                Method getComponentType = componentClass.getMethod("getComponentType");
                Object componentType = getComponentType.invoke(null);

                for (var method : store.getClass().getMethods()) {
                    if ("getComponent".equals(method.getName()) && method.getParameterCount() == 2) {
                        Object component = method.invoke(store, ref, componentType);
                        if (component != null) {
                            Method getInventory = component.getClass().getMethod("getInventory");
                            Object inventory = getInventory.invoke(component);
                            if (inventory instanceof Inventory result) {
                                return result;
                            }
                        }
                        break;
                    }
                }
            } catch (Exception _) {
            }
        }
        return null;
    }

    public static int countItem(Inventory inventory, String itemId) {
        if (inventory == null || itemId == null) return 0;
        int count = 0;
        count += countInContainer(inventory.getStorage(), itemId);
        count += countInContainer(inventory.getHotbar(), itemId);
        count += countInContainer(inventory.getArmor(), itemId);
        return count;
    }

    public static ItemStack getSlot(ItemContainer container, int slot) {
        return container.getItemStack((short) slot);
    }

    public static void clearInventory(Inventory inventory) {
        if (inventory != null) inventory.clear();
    }

    public static boolean addToStorage(Inventory inventory, ItemStack stack) {
        ItemContainer storage = inventory.getStorage();
        short capacity = storage.getCapacity();
        for (short i = 0; i < capacity; i++) {
            if (storage.canAddItemStackToSlot(i, stack, false, false)) {
                storage.addItemStackToSlot(i, stack);
                return true;
            }
        }
        return false;
    }

    private static int countInContainer(ItemContainer container, String itemId) {
        if (container == null) return 0;
        int count = 0;
        short capacity = container.getCapacity();
        for (short i = 0; i < capacity; i++) {
            ItemStack stack = container.getItemStack(i);
            if (stack != null && !stack.isEmpty() && itemId.equals(stack.getItemId())) {
                count += stack.getQuantity();
            }
        }
        return count;
    }
}
