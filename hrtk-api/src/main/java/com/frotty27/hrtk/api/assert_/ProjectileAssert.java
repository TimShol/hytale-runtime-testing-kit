package com.frotty27.hrtk.api.assert_;

/**
 * Assertions for projectile system availability.
 *
 * <p>Uses reflection to check whether the ProjectileModule class is present
 * on the classpath without importing HytaleServer.jar directly.</p>
 *
 * <pre>{@code
 * ProjectileAssert.assertProjectileModuleAvailable();
 * }</pre>
 *
 * @see HytaleAssert
 * @since 1.0.0
 */
public final class ProjectileAssert {

    private ProjectileAssert() {}

    /**
     * Asserts that the ProjectileModule class is available on the classpath.
     *
     * <p>Searches common package paths for the ProjectileModule class using
     * {@link Class#forName(String)}.</p>
     *
     * <p>Failure message: {@code "Expected ProjectileModule to be available on the classpath but it was not found"}</p>
     *
     * @throws AssertionFailedException if the ProjectileModule class cannot be located
     */
    public static void assertProjectileModuleAvailable() {
        String[] candidates = {
                "com.hypixel.hytale.server.projectile.ProjectileModule",
                "com.hypixel.hytale.server.ecs.module.ProjectileModule",
                "com.hypixel.hytale.server.module.ProjectileModule"
        };
        for (String className : candidates) {
            try {
                Class.forName(className);
                return;
            } catch (ClassNotFoundException ignored) {
            }
        }
        HytaleAssert.fail("Expected ProjectileModule to be available on the classpath but it was not found");
    }
}
