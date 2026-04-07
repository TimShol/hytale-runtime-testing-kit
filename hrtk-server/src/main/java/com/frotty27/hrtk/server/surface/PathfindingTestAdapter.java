package com.frotty27.hrtk.server.surface;

public final class PathfindingTestAdapter {

    private static final String ASTAR_BASE = "com.hypixel.hytale.server.npc.navigation.AStarBase";
    private static final String ASTAR_EVALUATOR = "com.hypixel.hytale.server.npc.navigation.AStarEvaluator";
    private static final String PATH_FOLLOWER = "com.hypixel.hytale.server.npc.navigation.PathFollower";

    private PathfindingTestAdapter() {}

    public static boolean pathfindingAvailable() {
        try {
            Class.forName(ASTAR_BASE);
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    public static boolean astarEvaluatorAvailable() {
        try {
            Class.forName(ASTAR_EVALUATOR);
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    public static boolean pathFollowerAvailable() {
        try {
            Class.forName(PATH_FOLLOWER);
            return true;
        } catch (Exception _) {
            return false;
        }
    }
}
