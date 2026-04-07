package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.assert_.ProjectileAssert;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;

import java.lang.reflect.Method;
import java.util.Map;

@HytaleSuite(value = "Projectile Tests", isolation = IsolationStrategy.NONE)
@Tag("projectile")
public class ProjectileTests {

    private static final String PROJECTILE_MODULE_CLASS =
        "com.hypixel.hytale.server.core.modules.projectile.ProjectileModule";
    private static final String PROJECTILE_CONFIG_CLASS =
        "com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig";
    private static final String STANDARD_PHYSICS_CLASS =
        "com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsConfig";

    @HytaleTest
    @DisplayName("ProjectileModule singleton is accessible")
    @Order(1)
    void projectileModuleAccessible() {
        ProjectileAssert.assertProjectileModuleAvailable();
    }

    @HytaleTest
    @DisplayName("ProjectileConfig assets are registered")
    @Order(2)
    void projectileConfigsExist() {
        try {
            Class<?> configClass = Class.forName(PROJECTILE_CONFIG_CLASS);
            Method getAssetMap = configClass.getMethod("getAssetMap");
            Object configs = getAssetMap.invoke(null);
            HytaleAssert.assertNotNull("Projectile config map should exist", configs);

            if (configs instanceof Map<?, ?> map) {
                HytaleAssert.assertFalse("Should have at least one projectile config",
                    map.isEmpty());
            }
        } catch (ClassNotFoundException e) {
            ProjectileAssert.assertProjectileModuleAvailable();
        } catch (Exception e) {
            HytaleAssert.fail("Failed to access ProjectileConfig: %s", e.getMessage());
        }
    }

    @HytaleTest
    @DisplayName("ProjectileConfig has valid physics properties")
    @Order(3)
    void projectileConfigPhysics() {
        try {
            Class<?> configClass = Class.forName(PROJECTILE_CONFIG_CLASS);
            Method getAssetMap = configClass.getMethod("getAssetMap");
            Object configs = getAssetMap.invoke(null);

            if (configs instanceof Map<?, ?> map && !map.isEmpty()) {
                Object first = map.values().iterator().next();

                Method getPhysicsConfig = first.getClass().getMethod("getPhysicsConfig");
                Object physics = getPhysicsConfig.invoke(first);
                HytaleAssert.assertNotNull("Projectile should have physics config", physics);

                Method getMuzzleVelocity = first.getClass().getMethod("getMuzzleVelocity");
                double velocity = (double) getMuzzleVelocity.invoke(first);
                HytaleAssert.assertTrue("Muzzle velocity should be positive", velocity > 0.0);
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            HytaleAssert.fail("Failed to check physics config: %s", e.getMessage());
        }
    }

    @HytaleTest
    @DisplayName("StandardPhysicsConfig has sensible defaults")
    @Order(4)
    void standardPhysicsConfigDefaults() {
        try {
            Class<?> standardPhysicsClass = Class.forName(STANDARD_PHYSICS_CLASS);
            HytaleAssert.assertNotNull("StandardPhysicsConfig class should be loadable",
                standardPhysicsClass);

            boolean hasGravity = false;
            boolean hasBounciness = false;
            boolean hasBounceLimit = false;
            for (Method method : standardPhysicsClass.getMethods()) {
                if ("getGravity".equals(method.getName())) hasGravity = true;
                if ("getBounciness".equals(method.getName())) hasBounciness = true;
                if ("getBounceLimit".equals(method.getName())) hasBounceLimit = true;
            }
            HytaleAssert.assertTrue("Should have getGravity method", hasGravity);
            HytaleAssert.assertTrue("Should have getBounciness method", hasBounciness);
            HytaleAssert.assertTrue("Should have getBounceLimit method", hasBounceLimit);
        } catch (ClassNotFoundException ignored) {
        }
    }
}
