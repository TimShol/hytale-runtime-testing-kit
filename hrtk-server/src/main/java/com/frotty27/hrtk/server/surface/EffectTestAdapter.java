package com.frotty27.hrtk.server.surface;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.effect.ActiveEntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;

public final class EffectTestAdapter {

    private EffectTestAdapter() {}

    public static boolean hasEffect(Store<EntityStore> store, Ref<EntityStore> ref, int effectIndex) {
        EffectControllerComponent controller = store.getComponent(ref, EffectControllerComponent.getComponentType());
        return controller != null && controller.hasEffect(effectIndex);
    }

    public static int getEffectCount(Store<EntityStore> store, Ref<EntityStore> ref) {
        EffectControllerComponent controller = store.getComponent(ref, EffectControllerComponent.getComponentType());
        if (controller == null) return 0;
        var activeEffects = controller.getActiveEffects();
        return activeEffects != null ? activeEffects.size() : 0;
    }

    public static List<Integer> getActiveEffectIndexes(Store<EntityStore> store, Ref<EntityStore> ref) {
        List<Integer> indexes = new ArrayList<>();
        EffectControllerComponent controller = store.getComponent(ref, EffectControllerComponent.getComponentType());
        if (controller == null) return indexes;
        int[] effectIndexes = controller.getActiveEffectIndexes();
        if (effectIndexes == null) return indexes;
        for (int index : effectIndexes) {
            indexes.add(index);
        }
        return indexes;
    }

    public static boolean isInvulnerable(Store<EntityStore> store, Ref<EntityStore> ref) {
        EffectControllerComponent controller = store.getComponent(ref, EffectControllerComponent.getComponentType());
        return controller != null && controller.isInvulnerable();
    }

    public static void applyEffect(Ref<EntityStore> ref, EntityEffect effect,
                                    ComponentAccessor<EntityStore> accessor) {
        EffectControllerComponent controller = accessor.getComponent(ref, EffectControllerComponent.getComponentType());
        if (controller != null) {
            controller.addEffect(ref, effect, accessor);
        }
    }

    public static void removeEffect(Ref<EntityStore> ref, int effectIndex,
                                     ComponentAccessor<EntityStore> accessor) {
        EffectControllerComponent controller = accessor.getComponent(ref, EffectControllerComponent.getComponentType());
        if (controller != null) {
            controller.removeEffect(ref, effectIndex, accessor);
        }
    }

    public static void clearEffects(Ref<EntityStore> ref,
                                     ComponentAccessor<EntityStore> accessor) {
        EffectControllerComponent controller = accessor.getComponent(ref, EffectControllerComponent.getComponentType());
        if (controller != null) {
            controller.clearEffects(ref, accessor);
        }
    }
}
