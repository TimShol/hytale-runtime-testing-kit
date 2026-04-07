package com.frotty27.hrtk.server.surface;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class CombatTestAdapter {

    private CombatTestAdapter() {}

    public static void dealDamage(Ref<EntityStore> target, CommandBuffer<EntityStore> commandBuffer, float amount) {
        Damage damage = new Damage(Damage.NULL_SOURCE, 0, amount);
        DamageSystems.executeDamage(target, commandBuffer, damage);
    }

    public static void kill(Ref<EntityStore> target, CommandBuffer<EntityStore> commandBuffer) {
        Damage damage = new Damage(Damage.NULL_SOURCE, 0, 999999f);
        DeathComponent.tryAddComponent(commandBuffer, target, damage);
    }

    public static void respawn(Ref<EntityStore> target, ComponentAccessor<EntityStore> accessor) {
        DeathComponent.respawn(accessor, target);
    }

    public static boolean isDead(Store<EntityStore> store, Ref<EntityStore> ref) {
        return StatsTestAdapter.isDead(store, ref);
    }

    public static boolean isAlive(Store<EntityStore> store, Ref<EntityStore> ref) {
        return !StatsTestAdapter.isDead(store, ref);
    }

    public static void dealDamageWithCause(Ref<EntityStore> target, CommandBuffer<EntityStore> commandBuffer,
                                            float amount, int causeIndex) {
        Damage damage = new Damage(Damage.NULL_SOURCE, causeIndex, amount);
        DamageSystems.executeDamage(target, commandBuffer, damage);
    }

    public static DeathComponent getDeathComponent(Store<EntityStore> store, Ref<EntityStore> ref) {
        return store.getComponent(ref, DeathComponent.getComponentType());
    }
}
