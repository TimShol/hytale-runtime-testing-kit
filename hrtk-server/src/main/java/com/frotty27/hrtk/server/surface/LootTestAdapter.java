package com.frotty27.hrtk.server.surface;

import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;

public final class LootTestAdapter {

    private LootTestAdapter() {}

    public static ItemDropList getDropList(String dropListId) {
        try {
            var assetMap = ItemDropList.getAssetMap();
            for (var method : assetMap.getClass().getMethods()) {
                if ("get".equals(method.getName()) && method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == Object.class) {
                    Object result = method.invoke(assetMap, dropListId);
                    if (result instanceof ItemDropList dropList) return dropList;
                }
            }
            for (var method : assetMap.getClass().getMethods()) {
                if ("getAsset".equals(method.getName()) && method.getParameterCount() == 1) {
                    Object result = method.invoke(assetMap, dropListId);
                    if (result instanceof ItemDropList dropList) return dropList;
                }
            }
        } catch (Exception _) {}
        return null;
    }

    public static boolean dropListExists(String dropListId) {
        return getDropList(dropListId) != null;
    }
}
