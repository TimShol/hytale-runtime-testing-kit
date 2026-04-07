package com.frotty27.hrtk.server.isolation;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TestWorldManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String TEST_WORLD_PREFIX = "_hrtk_test_";

    private final Map<String, String> suiteToWorldName = new ConcurrentHashMap<>();

    public World getOrCreateTestWorld(String suiteId) {
        Universe universe = Universe.get();

        String existingName = suiteToWorldName.get(suiteId);
        if (existingName != null) {
            World existing = universe.getWorld(existingName);
            if (existing != null) return existing;
            suiteToWorldName.remove(suiteId);
        }

        String worldName = TEST_WORLD_PREFIX + suiteId.hashCode() + "_" + System.currentTimeMillis();
        try {
            var future = universe.addWorld(worldName);
            World world = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
            if (world != null) {
                suiteToWorldName.put(suiteId, worldName);
                waitForWorldReady(world);
                ensureSpawnChunkLoaded(world);
                LOGGER.atInfo().log("HRTK: Created test world '%s' for suite '%s'", worldName, suiteId);
                return world;
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("HRTK: Failed to create test world '%s': %s - falling back to existing world",
                    worldName, e.getMessage());
        }

        Map<String, World> worlds = universe.getWorlds();
        if (worlds != null && !worlds.isEmpty()) {
            World fallback = worlds.values().iterator().next();
            suiteToWorldName.put(suiteId, fallback.getName());
            return fallback;
        }

        LOGGER.atSevere().log("HRTK: No worlds available for test execution");
        return null;
    }

    public void cleanupTestWorld(String suiteId) {
        String worldName = suiteToWorldName.remove(suiteId);
        if (worldName == null || !isTestWorld(worldName)) return;

        try {
            Path worldsRoot = Universe.get().getWorldsPath();
            Path savePath = resolveAndValidateSavePath(worldName, worldsRoot);

            boolean removed = Universe.get().removeWorld(worldName);
            if (removed) {
                LOGGER.atInfo().log("HRTK: Removed test world '%s'", worldName);
            }

            if (savePath != null) {
                try { Thread.sleep(500); } catch (InterruptedException _) {}
                deleteSaveFolder(savePath, worldsRoot);
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("HRTK: Failed to remove test world '%s': %s", worldName, e.getMessage());
        }
    }

    private Path resolveAndValidateSavePath(String worldName, Path worldsRoot) {
        if (worldsRoot == null) return null;

        Path savePath = null;
        try {
            World world = Universe.get().getWorld(worldName);
            if (world != null) savePath = world.getSavePath();
        } catch (Exception _) {}

        if (savePath == null) {
            savePath = worldsRoot.resolve(worldName);
        }

        savePath = savePath.toAbsolutePath().normalize();
        Path normalizedRoot = worldsRoot.toAbsolutePath().normalize();

        if (!savePath.startsWith(normalizedRoot)) {
            LOGGER.atSevere().log("HRTK: SAFETY CHECK FAILED - save path '%s' is outside worlds root '%s'. Refusing to delete.",
                    savePath, normalizedRoot);
            return null;
        }

        String folderName = savePath.getFileName().toString();
        if (!folderName.startsWith(TEST_WORLD_PREFIX)) {
            LOGGER.atSevere().log("HRTK: SAFETY CHECK FAILED - folder name '%s' does not start with '%s'. Refusing to delete.",
                    folderName, TEST_WORLD_PREFIX);
            return null;
        }

        return savePath;
    }

    private void deleteSaveFolder(Path savePath, Path worldsRoot) {
        if (!Files.exists(savePath)) return;

        Path normalizedPath = savePath.toAbsolutePath().normalize();
        Path normalizedRoot = worldsRoot.toAbsolutePath().normalize();

        if (!normalizedPath.startsWith(normalizedRoot)) {
            LOGGER.atSevere().log("HRTK: SAFETY CHECK FAILED at deletion time - '%s' is outside '%s'", normalizedPath, normalizedRoot);
            return;
        }
        if (!normalizedPath.getFileName().toString().startsWith(TEST_WORLD_PREFIX)) {
            LOGGER.atSevere().log("HRTK: SAFETY CHECK FAILED at deletion time - '%s' is not an HRTK test world", normalizedPath.getFileName());
            return;
        }

        try (var stream = Files.walk(normalizedPath)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); } catch (IOException _) {}
            });
            LOGGER.atInfo().log("HRTK: Deleted test world save folder '%s'", normalizedPath);
        } catch (IOException e) {
            LOGGER.atWarning().log("HRTK: Failed to delete test world save folder '%s': %s", normalizedPath, e.getMessage());
        }
    }

    public void cleanupAllTestWorlds() {
        for (String suiteId : List.copyOf(suiteToWorldName.keySet())) {
            cleanupTestWorld(suiteId);
        }
    }

    private void waitForWorldReady(World world) {
        for (int i = 0; i < 100; i++) {
            if (world.isAlive() && world.isTicking()) return;
            try { Thread.sleep(50); } catch (InterruptedException _) { return; }
        }
        LOGGER.atWarning().log("HRTK: Test world '%s' did not become ready within 5 seconds", world.getName());
    }

    private void ensureSpawnChunkLoaded(World world) {
        try {
            long spawnChunkKey = 0L;
            var chunkFuture = world.getChunkAsync(spawnChunkKey);
            if (chunkFuture != null) {
                chunkFuture.get(10, java.util.concurrent.TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            LOGGER.atWarning().log("HRTK: Failed to pre-load spawn chunk for test world '%s': %s", world.getName(), e.getMessage());
        }
    }

    public static boolean isTestWorld(String worldName) {
        return worldName != null && worldName.startsWith(TEST_WORLD_PREFIX);
    }
}
