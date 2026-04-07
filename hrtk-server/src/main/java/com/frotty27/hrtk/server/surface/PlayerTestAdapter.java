package com.frotty27.hrtk.server.surface;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;

public final class PlayerTestAdapter {

    private PlayerTestAdapter() {}

    public static Player getPlayer(World world, String name) {
        try {
            if (world == null || name == null) return null;
            for (Player player : world.getPlayers()) {
                if (name.equals(player.getDisplayName())) {
                    return player;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static PlayerRef getPlayerRef(World world, String name) {
        try {
            Player player = getPlayer(world, name);
            return player != null ? player.getPlayerRef() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getGameMode(Player player) {
        try {
            if (player == null) return null;
            Object mode = player.getGameMode();
            return mode != null ? mode.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getWorldName(Player player) {
        try {
            if (player == null) return null;
            PlayerRef ref = player.getPlayerRef();
            return ref != null ? ref.getUsername() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
