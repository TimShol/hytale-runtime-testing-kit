package com.frotty27.hrtk.server.surface;

import com.hypixel.hytale.server.core.inventory.ItemStack;

public final class ItemTestAdapter {

    private ItemTestAdapter() {}

    public static ItemStack createStack(String itemId, int quantity) {
        try {
            return new ItemStack(itemId, quantity);
        } catch (Exception e) {
            return null;
        }
    }

    public static ItemStack createStack(String itemId) {
        try {
            return new ItemStack(itemId);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getItemId(ItemStack stack) {
        try {
            return stack != null ? stack.getItemId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static int getQuantity(ItemStack stack) {
        try {
            return stack != null ? stack.getQuantity() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean isEmpty(ItemStack stack) {
        try {
            return stack == null || stack.isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isBroken(ItemStack stack) {
        try {
            return stack != null && stack.isBroken();
        } catch (Exception e) {
            return false;
        }
    }

    public static double getDurability(ItemStack stack) {
        try {
            return stack != null ? stack.getDurability() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static double getMaxDurability(ItemStack stack) {
        try {
            return stack != null ? stack.getMaxDurability() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static ItemStack withDurability(ItemStack stack, double durability) {
        try {
            return stack != null ? stack.withDurability(durability) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isStackable(ItemStack a, ItemStack b) {
        try {
            return a != null && b != null && a.isStackableWith(b);
        } catch (Exception e) {
            return false;
        }
    }
}
