package com.frotty27.hrtk.server.surface;

import java.lang.reflect.Method;

public final class ChunkTestAdapter {

    private static final String WORLD_CLASS = "com.hypixel.hytale.server.core.universe.world.World";
    private static final String CHUNK_COLUMN_CLASS = "com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn";

    private ChunkTestAdapter() {}

    public static boolean isChunkLoaded(Object world, int chunkX, int chunkZ) {
        try {
            if (world == null) return false;
            for (Method m : world.getClass().getMethods()) {
                if ("getChunkIfLoaded".equals(m.getName()) && m.getParameterCount() == 2) {
                    Object chunk = m.invoke(world, chunkX, chunkZ);
                    return chunk != null;
                }
                if ("getChunkColumn".equals(m.getName()) && m.getParameterCount() == 2) {
                    Object chunk = m.invoke(world, chunkX, chunkZ);
                    return chunk != null;
                }
            }
            return false;
        } catch (Exception _) {
            return false;
        }
    }

    public static int getChunkEntityCount(Object world, int chunkX, int chunkZ) {
        try {
            if (world == null) return -1;
            Object chunk = null;
            for (Method m : world.getClass().getMethods()) {
                if ("getChunkIfLoaded".equals(m.getName()) && m.getParameterCount() == 2) {
                    chunk = m.invoke(world, chunkX, chunkZ);
                    break;
                }
                if ("getChunkColumn".equals(m.getName()) && m.getParameterCount() == 2) {
                    chunk = m.invoke(world, chunkX, chunkZ);
                    break;
                }
            }
            if (chunk == null) return -1;
            for (Method m : chunk.getClass().getMethods()) {
                if ("getEntityCount".equals(m.getName()) && m.getParameterCount() == 0) {
                    Object result = m.invoke(chunk);
                    return result instanceof Number n ? n.intValue() : -1;
                }
            }
            return -1;
        } catch (Exception _) {
            return -1;
        }
    }

    public static boolean chunkClassesAvailable() {
        try {
            Class.forName(WORLD_CLASS);
            Class.forName(CHUNK_COLUMN_CLASS);
            return true;
        } catch (Exception _) {
            return false;
        }
    }
}
