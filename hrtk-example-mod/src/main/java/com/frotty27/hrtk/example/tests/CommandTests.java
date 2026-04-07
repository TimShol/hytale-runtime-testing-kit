package com.frotty27.hrtk.example.tests;

import com.frotty27.hrtk.api.annotation.DisplayName;
import com.frotty27.hrtk.api.annotation.HytaleSuite;
import com.frotty27.hrtk.api.annotation.HytaleTest;
import com.frotty27.hrtk.api.annotation.Order;
import com.frotty27.hrtk.api.annotation.RequiresPlayer;
import com.frotty27.hrtk.api.annotation.Tag;
import com.frotty27.hrtk.api.assert_.CommandAssert;
import com.frotty27.hrtk.api.assert_.HytaleAssert;
import com.frotty27.hrtk.api.context.TestContext;
import com.frotty27.hrtk.api.lifecycle.IsolationStrategy;
import com.hypixel.hytale.server.core.command.system.CommandManager;

@HytaleSuite(value = "Command Tests", isolation = IsolationStrategy.NONE)
@Tag("commands")
public class CommandTests {

    @HytaleTest
    @RequiresPlayer
    @DisplayName("Custom command executes without error")
    @Order(1)
    void testCommandSucceeds(TestContext ctx) {
        var sender = ctx.createCommandSender("mymod.use");

        CommandAssert.assertCommandSucceeds(ctx, sender, "/mycommand help");
        CommandAssert.assertSenderReceivedMessage(sender, "Usage");
    }

    @HytaleTest
    @RequiresPlayer
    @DisplayName("Command fails without required permission")
    @Order(2)
    void testCommandDenied(TestContext ctx) {
        var sender = ctx.createCommandSender();

        CommandAssert.assertCommandFails(ctx, sender, "/admin-only-command");
    }

    @HytaleTest
    @RequiresPlayer
    @DisplayName("Command sends correct feedback to sender")
    @Order(3)
    void testCommandOutput(TestContext ctx) {
        var sender = ctx.createCommandSender("mymod.heal");

        CommandAssert.assertCommandSucceeds(ctx, sender, "/heal");
        CommandAssert.assertSenderReceivedMessage(sender, "healed");
        CommandAssert.assertSenderReceivedMessageCount(sender, 1);
    }

    @HytaleTest
    @DisplayName("CommandManager is accessible and has registrations")
    @Order(4)
    void testCommandManagerAccess() {
        var manager = CommandManager.get();
        HytaleAssert.assertNotNull("CommandManager should not be null", manager);

        var registration = manager.getCommandRegistration();
        HytaleAssert.assertNotNull("Command registration should not be null", registration);
    }

    @HytaleTest
    @RequiresPlayer
    @DisplayName("Command failure message contains useful context")
    @Order(5)
    void testCommandFailureMessage(TestContext ctx) {
        var sender = ctx.createCommandSender();

        CommandAssert.assertCommandFailsWithMessage(ctx, sender, "/bad args", "Invalid");
    }
}
