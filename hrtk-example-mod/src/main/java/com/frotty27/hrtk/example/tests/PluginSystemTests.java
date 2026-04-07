package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.plugin.PluginState;

@HytaleSuite(value = "Plugin System Tests", isolation = IsolationStrategy.NONE)
@Tag("plugin")
public class PluginSystemTests {

    @HytaleTest
    @DisplayName("PluginManager is accessible and has plugins loaded")
    @Order(1)
    void pluginManagerAccessible() {
        var manager = PluginManager.get();
        HytaleAssert.assertNotNull("PluginManager should not be null", manager);

        var plugins = manager.getPlugins();
        HytaleAssert.assertNotNull("Plugin list should not be null", plugins);
        HytaleAssert.assertNotEmpty(plugins);
    }

    @HytaleTest
    @DisplayName("Loaded plugins have valid identifiers and state")
    @Order(2)
    void loadedPluginsValid() {
        var plugins = PluginManager.get().getPlugins();
        for (var plugin : plugins) {
            HytaleAssert.assertNotNull("Plugin name should not be null", plugin.getName());
            HytaleAssert.assertNotNull("Plugin identifier should not be null",
                plugin.getIdentifier());
            HytaleAssert.assertTrue("Plugin should be enabled or disabled",
                plugin.isEnabled() || plugin.isDisabled());
        }
    }

    @HytaleTest
    @DisplayName("PluginState lifecycle values exist")
    @Order(3)
    void pluginStateLifecycleValues() {
        HytaleAssert.assertNotNull(PluginState.NONE);
        HytaleAssert.assertNotNull(PluginState.SETUP);
        HytaleAssert.assertNotNull(PluginState.START);
        HytaleAssert.assertNotNull(PluginState.ENABLED);
        HytaleAssert.assertNotNull(PluginState.SHUTDOWN);
        HytaleAssert.assertNotNull(PluginState.DISABLED);
        HytaleAssert.assertNotNull(PluginState.FAILED);
        HytaleAssert.assertEquals(7, PluginState.values().length);
    }

    @HytaleTest
    @DisplayName("PermissionsModule is accessible from plugin context")
    @Order(4)
    void permissionsModuleFromPluginContext() {
        var module = PermissionsModule.get();
        HytaleAssert.assertNotNull("PermissionsModule should be accessible", module);
    }

    @HytaleTest
    @DisplayName("Built-in permission constants are accessible")
    @Order(5)
    void permissionConstantsAccessible() {
        HytaleAssert.assertNotNull("COMMAND_BASE should exist",
            HytalePermissions.COMMAND_BASE);
        HytaleAssert.assertNotNull("ASSET_EDITOR should exist",
            HytalePermissions.ASSET_EDITOR);
        HytaleAssert.assertNotNull("BUILDER_TOOLS_EDITOR should exist",
            HytalePermissions.BUILDER_TOOLS_EDITOR);
        HytaleAssert.assertNotNull("FLY_CAM should exist",
            HytalePermissions.FLY_CAM);
    }

    @HytaleTest
    @DisplayName("PermissionProvider is registered in module")
    @Order(6)
    void permissionProviderRegistered() {
        var module = PermissionsModule.get();
        HytaleAssert.assertNotNull("PermissionsModule should not be null", module);

        var providers = module.getProviders();
        HytaleAssert.assertNotNull("Providers should not be null", providers);
        HytaleAssert.assertNotEmpty(providers);
    }
}
