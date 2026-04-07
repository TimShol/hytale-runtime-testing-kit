package com.frotty27.hrtk.server.surface;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class PhysicsTestAdapter {

    private PhysicsTestAdapter() {}

    public static Velocity getVelocity(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            return store.getComponent(ref, Velocity.getComponentType());
        } catch (Exception e) {
            return null;
        }
    }

    public static double getVelocityX(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            Velocity velocity = getVelocity(store, ref);
            return velocity != null ? velocity.getX() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static double getVelocityY(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            Velocity velocity = getVelocity(store, ref);
            return velocity != null ? velocity.getY() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static double getVelocityZ(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            Velocity velocity = getVelocity(store, ref);
            return velocity != null ? velocity.getZ() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static double getSpeed(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            Velocity velocity = getVelocity(store, ref);
            return velocity != null ? velocity.getSpeed() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static void setVelocity(Store<EntityStore> store, Ref<EntityStore> ref, double x, double y, double z) {
        try {
            Velocity velocity = getVelocity(store, ref);
            if (velocity != null) {
                velocity.set(x, y, z);
            }
        } catch (Exception ignored) {}
    }

    public static void addForce(Store<EntityStore> store, Ref<EntityStore> ref, double x, double y, double z) {
        try {
            Velocity velocity = getVelocity(store, ref);
            if (velocity != null) {
                velocity.addForce(x, y, z);
            }
        } catch (Exception ignored) {}
    }
}
