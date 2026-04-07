package com.frotty27.hrtk.server.surface;

import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class WorldTestAdapter {

    private WorldTestAdapter() {}

    public static World getWorld(String name) {
        return Universe.get().getWorld(name);
    }

    public static World getAnyWorld() {
        Map<String, World> worlds = Universe.get().getWorlds();
        if (worlds == null || worlds.isEmpty()) return null;
        return worlds.values().iterator().next();
    }

    public static boolean worldExists(String name) {
        Map<String, World> worlds = Universe.get().getWorlds();
        return worlds != null && worlds.containsKey(name);
    }

    public static CompletableFuture<World> createWorld(String name) {
        return Universe.get().addWorld(name);
    }

    public static boolean removeWorld(String name) {
        return Universe.get().removeWorld(name);
    }
}
