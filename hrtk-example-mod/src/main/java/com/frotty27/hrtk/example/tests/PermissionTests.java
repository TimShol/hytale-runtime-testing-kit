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

import java.util.Set;
import java.util.UUID;

@HytaleSuite(value = "Permission Tests", isolation = IsolationStrategy.NONE)
@Tag("permissions")
public class PermissionTests {

    @HytaleTest
    @DisplayName("PermissionsModule singleton is accessible")
    @Order(1)
    void permissionsModuleAccessible() {
        var module = PermissionsModule.get();
        HytaleAssert.assertNotNull("PermissionsModule.get() should not return null", module);
    }

    @HytaleTest
    @DisplayName("Added permission is immediately queryable")
    @Order(2)
    void addedPermissionQueryable() {
        var module = PermissionsModule.get();
        var testUuid = UUID.randomUUID();

        module.addUserPermission(testUuid, Set.of("test.permission"));
        HytaleAssert.assertTrue("Permission should be active after add",
            module.hasPermission(testUuid, "test.permission"));

        module.removeUserPermission(testUuid, Set.of("test.permission"));
        HytaleAssert.assertFalse("Permission should be gone after remove",
            module.hasPermission(testUuid, "test.permission"));
    }

    @HytaleTest
    @DisplayName("User inherits group permissions")
    @Order(3)
    void userInheritsGroupPermissions() {
        var module = PermissionsModule.get();
        var testUuid = UUID.randomUUID();

        module.addGroupPermission("vip", Set.of("mymod.vip.feature"));
        module.addUserToGroup(testUuid, "vip");

        var groups = module.getGroupsForUser(testUuid);
        HytaleAssert.assertTrue("User should be in vip group", groups.contains("vip"));
        HytaleAssert.assertTrue("User should inherit group permission",
            module.hasPermission(testUuid, "mymod.vip.feature"));

        module.removeUserFromGroup(testUuid, "vip");
        module.removeGroupPermission("vip", Set.of("mymod.vip.feature"));
    }

    @HytaleTest
    @DisplayName("Built-in permission constants are defined")
    @Order(4)
    void builtInPermissionsExist() {
        HytaleAssert.assertNotNull("COMMAND_BASE should be defined",
            HytalePermissions.COMMAND_BASE);
        HytaleAssert.assertNotNull("ASSET_EDITOR should be defined",
            HytalePermissions.ASSET_EDITOR);
        HytaleAssert.assertNotNull("FLY_CAM should be defined",
            HytalePermissions.FLY_CAM);
    }

    @HytaleTest
    @DisplayName("PermissionProvider is registered and accessible")
    @Order(5)
    void permissionProviderExists() {
        var module = PermissionsModule.get();
        HytaleAssert.assertNotNull("PermissionsModule should not be null", module);

        var providers = module.getProviders();
        HytaleAssert.assertNotNull("Providers list should not be null", providers);
        HytaleAssert.assertNotEmpty(providers);

        var first = providers.iterator().next();
        HytaleAssert.assertNotNull("Provider should have a name", first.getName());
    }
}
