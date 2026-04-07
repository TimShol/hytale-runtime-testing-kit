package com.frotty27.hrtk.server.surface;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class StatsTestAdapter {

    private StatsTestAdapter() {}

    public static float getStatMax(Store<EntityStore> store, Ref<EntityStore> ref, int statIndex) {
        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) return 0f;
        EntityStatValue value = statMap.get(statIndex);
        return value != null ? value.getMax() : 0f;
    }

    public static float getStatValue(Store<EntityStore> store, Ref<EntityStore> ref, int statIndex) {
        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) return 0f;
        EntityStatValue value = statMap.get(statIndex);
        return value != null ? value.get() : 0f;
    }

    public static float getHealth(Store<EntityStore> store, Ref<EntityStore> ref) {
        return getStatValue(store, ref, DefaultEntityStatTypes.getHealth());
    }

    public static float getStamina(Store<EntityStore> store, Ref<EntityStore> ref) {
        return getStatValue(store, ref, DefaultEntityStatTypes.getStamina());
    }

    public static void setStatValue(Store<EntityStore> store, Ref<EntityStore> ref, int statIndex, float value) {
        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap != null) {
            statMap.setStatValue(statIndex, value);
        }
    }

    public static boolean isDead(Store<EntityStore> store, Ref<EntityStore> ref) {
        return store.getComponent(ref, DeathComponent.getComponentType()) != null;
    }

    public static EntityStatMap getStatMap(Store<EntityStore> store, Ref<EntityStore> ref) {
        return store.getComponent(ref, EntityStatMap.getComponentType());
    }

    public static float getMana(Store<EntityStore> store, Ref<EntityStore> ref) {
        return getStatValue(store, ref, DefaultEntityStatTypes.getMana());
    }

    public static float getOxygen(Store<EntityStore> store, Ref<EntityStore> ref) {
        return getStatValue(store, ref, DefaultEntityStatTypes.getOxygen());
    }

    public static void addModifier(Store<EntityStore> store, Ref<EntityStore> ref,
                                    int statIndex, String modifierId, Modifier modifier) {
        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap != null) {
            statMap.putModifier(statIndex, modifierId, modifier);
        }
    }

    public static void removeModifier(Store<EntityStore> store, Ref<EntityStore> ref,
                                       int statIndex, String modifierId) {
        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap != null) {
            statMap.removeModifier(statIndex, modifierId);
        }
    }
}
