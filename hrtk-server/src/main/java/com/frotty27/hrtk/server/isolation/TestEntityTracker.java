package com.frotty27.hrtk.server.isolation;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;

public final class TestEntityTracker {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Store<EntityStore> store;
    private final List<Ref<EntityStore>> trackedEntities = new java.util.concurrent.CopyOnWriteArrayList<>();

    public TestEntityTracker(Store<EntityStore> store) {
        this.store = store;
    }

    public void trackEntity(Ref<EntityStore> ref) {
        trackedEntities.add(ref);
    }

    public void restore() {
        int removed = 0;
        for (Ref<EntityStore> ref : trackedEntities) {
            try {
                store.removeEntity(ref, RemoveReason.REMOVE);
                removed++;
            } catch (Exception _) {
            }
        }
        trackedEntities.clear();
        if (removed > 0) {
            LOGGER.atInfo().log("HRTK: Snapshot rollback removed %d test entities", removed);
        }
    }

    public int getTrackedCount() {
        return trackedEntities.size();
    }
}
