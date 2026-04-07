package com.frotty27.hrtk.server.surface;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

public final class BlockTestAdapter {

    private BlockTestAdapter() {}

    public static BlockType getBlockType(String blockId) {
        try {
            BlockType type = BlockType.fromString(blockId);
            if (type != null) return type;
            return BlockType.getAssetMap().getAsset(blockId);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getBlockMaterial(BlockType blockType) {
        try {
            return blockType != null ? String.valueOf(blockType.getMaterial()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isTrigger(BlockType blockType) {
        try {
            return blockType != null && blockType.isTrigger();
        } catch (Exception e) {
            return false;
        }
    }

    public static Object getBlockState(BlockType blockType) {
        try {
            return blockType != null ? blockType.getDefaultStateKey() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getBlockGroup(BlockType blockType) {
        try {
            return blockType != null ? blockType.getGroup() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean blockTypeExists(String blockId) {
        try {
            return getBlockType(blockId) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
