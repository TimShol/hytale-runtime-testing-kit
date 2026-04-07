package com.frotty27.hrtk.server.surface;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;

import java.util.Collection;
import java.util.Collections;

public final class NPCTestAdapter {

    private NPCTestAdapter() {}

    public static NPCEntity getNPCEntity(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            return store.getComponent(ref, NPCEntity.getComponentType());
        } catch (Exception e) {
            return null;
        }
    }

    public static String getRoleName(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            NPCEntity npc = getNPCEntity(store, ref);
            if (npc == null) return null;
            Role role = npc.getRole();
            return role != null ? role.getRoleName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Role getRole(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            NPCEntity npc = getNPCEntity(store, ref);
            return npc != null ? npc.getRole() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isDespawning(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            NPCEntity npc = getNPCEntity(store, ref);
            return npc != null && npc.isDespawning();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean roleExists(String roleName) {
        try {
            return NPCPlugin.get().hasRoleName(roleName);
        } catch (Exception e) {
            return false;
        }
    }

    public static Collection<String> getRoleNames() {
        try {
            return NPCPlugin.get().getRoleTemplateNames(false);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static Object getLeashPoint(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            NPCEntity npc = getNPCEntity(store, ref);
            return npc != null ? npc.getLeashPoint() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
